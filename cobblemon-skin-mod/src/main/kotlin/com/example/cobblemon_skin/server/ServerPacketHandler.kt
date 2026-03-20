package com.example.cobblemon_skin.server

import com.cobblemon.mod.common.Cobblemon
import com.example.cobblemon_skin.CobblemonSkinMod
import com.example.cobblemon_skin.network.*
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import java.util.UUID

/**
 * Server-side packet handler. Registers C2S packet receivers and JOIN event.
 * Sends resource pack chunks via throttled tick-based delivery (streaming from disk).
 *
 * Key improvements over previous version:
 * - Chunks are read from disk per-tick (no memory pressure)
 * - Transfer only starts when client explicitly requests it (after a delay)
 * - Throttled to 1 chunk/tick (~5MB/sec) to avoid connection flooding
 */
object ServerPacketHandler {

    /** Transfer state per player: packType (0=resolver, 1=asset) + next chunk index. */
    private data class TransferState(val packType: Int, var chunkIndex: Int)
    private val pendingTransfers = mutableMapOf<UUID, TransferState>()

    /** How many 256KB chunks to send per server tick. 1 × 256KB × 20tps = ~5MB/sec. */
    private const val CHUNKS_PER_TICK = 1

    /** Reference to the server, set when first tick event fires. */
    @Volatile
    private var serverRef: net.minecraft.server.MinecraftServer? = null

    /**
     * Called by Bukkit plugin (via reflection) after PlayerJoinEvent with delay.
     * Sends skin list + pack hash to the specified player.
     */
    @JvmStatic
    fun sendSkinListToPlayer(playerUuid: UUID) {
        val server = serverRef ?: return
        val player = server.playerList.getPlayer(playerUuid) ?: return
        val resolverHash = ResourcePackTransfer.resolverPackHash
        val assetHash = ResourcePackTransfer.assetPackHash
        val skins = SkinManager.getSkinInfoList()
        CobblemonSkinMod.LOGGER.info("Sending skin list to ${player.name.string} (${skins.size} skins, resolver=$resolverHash, asset=$assetHash)")
        try {
            SkinPackets.sendSkinList(player, skins, resolverHash, assetHash)
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("Failed to send skin list to ${player.name.string}: ${e.message}")
        }
    }

    fun register() {
        // NO JOIN event handler — Bukkit plugin's PlayerJoinEvent triggers sendSkinListToPlayer()

        // Tick-based throttled chunk sending (reads from disk, low memory)
        ServerTickEvents.END_SERVER_TICK.register { server ->
            if (serverRef == null) serverRef = server
            if (pendingTransfers.isEmpty()) return@register
            val iterator = pendingTransfers.iterator()
            while (iterator.hasNext()) {
                val (uuid, state) = iterator.next()
                val player = server.playerList.getPlayer(uuid)
                if (player == null) {
                    iterator.remove()
                    continue
                }
                val totalChunks = ResourcePackTransfer.getTotalChunks(state.packType)
                val endIndex = minOf(state.chunkIndex + CHUNKS_PER_TICK, totalChunks)
                for (i in state.chunkIndex until endIndex) {
                    try {
                        val chunkData = ResourcePackTransfer.readChunk(state.packType, i)
                        ServerPlayNetworking.send(player, ResourcePackChunkS2C(state.packType, i, totalChunks, chunkData))
                    } catch (e: Exception) {
                        CobblemonSkinMod.LOGGER.error("Failed to send chunk $i (type=${state.packType}) to ${player.name.string}: ${e.message}")
                        iterator.remove()
                        break
                    }
                }
                if (endIndex >= totalChunks) {
                    val typeName = if (state.packType == 0) "resolver" else "asset"
                    CobblemonSkinMod.LOGGER.info("$typeName pack transfer complete for ${player.name.string}")
                    iterator.remove()
                } else {
                    state.chunkIndex = endIndex
                }
            }
        }

        // Handle C2S: resource pack download request — queue for tick-based delivery
        ServerPlayNetworking.registerGlobalReceiver(ResourcePackRequestC2S.ID) { payload: ResourcePackRequestC2S, context: ServerPlayNetworking.Context ->
            val player = context.player()
            if (!ResourcePackTransfer.isReady()) {
                CobblemonSkinMod.LOGGER.warn("Resource pack not ready for ${player.name.string}")
                return@registerGlobalReceiver
            }

            val packType = payload.packType
            val totalChunks = ResourcePackTransfer.getTotalChunks(packType)
            val typeName = if (packType == 0) "resolver" else "asset"
            CobblemonSkinMod.LOGGER.info("Queuing $typeName pack transfer for ${player.name.string} ($totalChunks chunks)")
            pendingTransfers[player.uuid] = TransferState(packType, 0)
        }

        // Handle C2S: skin resource request (on-demand per-skin files)
        ServerPlayNetworking.registerGlobalReceiver(SkinResourceRequest.ID) { payload: SkinResourceRequest, context: ServerPlayNetworking.Context ->
            val player = context.player()
            val skinId = payload.skinId
            CobblemonSkinMod.LOGGER.info("Player ${player.name.string} requested skin files: $skinId")

            val files = ResourcePackTransfer.getSkinFiles(skinId)
            if (files.isNotEmpty()) {
                SkinPackets.sendSkinResource(player, SkinResourceData(skinId, files))
                CobblemonSkinMod.LOGGER.info("Sent ${files.size} files for skin '$skinId' to ${player.name.string}")
            } else {
                CobblemonSkinMod.LOGGER.warn("No files found for skin: $skinId")
            }
        }

        // Handle C2S: skin apply request
        ServerPlayNetworking.registerGlobalReceiver(SkinApplyRequest.ID) { payload: SkinApplyRequest, context: ServerPlayNetworking.Context ->
            val player = context.player()
            val slot = payload.slot
            val skinId = payload.skinId

            try {
                val party = Cobblemon.INSTANCE.getStorage().getParty(player)
                val pokemon = CobblemonSkinMod.getPartyPokemon(party, slot)
                if (pokemon != null) {
                    CobblemonSkinMod.applySkin(pokemon, skinId)
                    CobblemonSkinMod.LOGGER.info("Applied skin '$skinId' to ${player.name.string}'s slot $slot")
                    SkinPackets.broadcastSkinApply(player.server!!, player.uuid, slot, skinId)
                }
            } catch (e: Exception) {
                CobblemonSkinMod.LOGGER.error("Failed to apply skin: ${e.message}")
            }
        }

        // Handle C2S: skin clear request
        ServerPlayNetworking.registerGlobalReceiver(SkinClearRequest.ID) { payload: SkinClearRequest, context: ServerPlayNetworking.Context ->
            val player = context.player()
            val slot = payload.slot

            try {
                val party = Cobblemon.INSTANCE.getStorage().getParty(player)
                val pokemon = CobblemonSkinMod.getPartyPokemon(party, slot)
                if (pokemon != null) {
                    CobblemonSkinMod.clearSkin(pokemon)
                    CobblemonSkinMod.LOGGER.info("Cleared skin from ${player.name.string}'s slot $slot")
                    SkinPackets.broadcastSkinApply(player.server!!, player.uuid, slot, "")
                }
            } catch (e: Exception) {
                CobblemonSkinMod.LOGGER.error("Failed to clear skin: ${e.message}")
            }
        }
    }
}
