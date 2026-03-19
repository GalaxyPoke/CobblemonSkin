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

    /** Pending resource pack transfers: player UUID → next chunk index to send. */
    private val pendingTransfers = mutableMapOf<UUID, Int>()

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
        val packHash = ResourcePackTransfer.packHash
        val skins = SkinManager.getSkinInfoList()
        CobblemonSkinMod.LOGGER.info("Sending skin list to ${player.name.string} (${skins.size} skins, pack=$packHash)")
        try {
            SkinPackets.sendSkinList(player, skins, packHash)
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
            val totalChunks = ResourcePackTransfer.getTotalChunks()
            val iterator = pendingTransfers.iterator()
            while (iterator.hasNext()) {
                val (uuid, startIndex) = iterator.next()
                val player = server.playerList.getPlayer(uuid)
                if (player == null) {
                    iterator.remove()
                    continue
                }
                val endIndex = minOf(startIndex + CHUNKS_PER_TICK, totalChunks)
                for (i in startIndex until endIndex) {
                    try {
                        val chunkData = ResourcePackTransfer.readChunk(i)
                        ServerPlayNetworking.send(player, ResourcePackChunkS2C(i, totalChunks, chunkData))
                    } catch (e: Exception) {
                        CobblemonSkinMod.LOGGER.error("Failed to send chunk $i to ${player.name.string}: ${e.message}")
                        iterator.remove()
                        break
                    }
                }
                if (endIndex >= totalChunks) {
                    CobblemonSkinMod.LOGGER.info("Resource pack transfer complete for ${player.name.string}")
                    iterator.remove()
                } else {
                    pendingTransfers[uuid] = endIndex
                }
            }
        }

        // Handle C2S: resource pack download request — queue for tick-based delivery
        ServerPlayNetworking.registerGlobalReceiver(ResourcePackRequestC2S.ID) { _: ResourcePackRequestC2S, context: ServerPlayNetworking.Context ->
            val player = context.player()
            if (!ResourcePackTransfer.isReady()) {
                CobblemonSkinMod.LOGGER.warn("Resource pack not ready for ${player.name.string}")
                return@registerGlobalReceiver
            }

            val totalChunks = ResourcePackTransfer.getTotalChunks()
            CobblemonSkinMod.LOGGER.info("Queuing resource pack transfer for ${player.name.string} ($totalChunks chunks)")
            pendingTransfers[player.uuid] = 0
        }

        // Handle C2S: skin resource request (on-demand, kept for compatibility)
        ServerPlayNetworking.registerGlobalReceiver(SkinResourceRequest.ID) { payload: SkinResourceRequest, context: ServerPlayNetworking.Context ->
            val player = context.player()
            val skinId = payload.skinId
            CobblemonSkinMod.LOGGER.debug("Player ${player.name.string} requested skin resource: $skinId")

            val data = SkinManager.getSkinResourceData(skinId)
            if (data != null) {
                SkinPackets.sendSkinResource(player, data)
            } else {
                CobblemonSkinMod.LOGGER.warn("Skin resource not found: $skinId")
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
