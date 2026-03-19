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
 * Uses tick-based throttling for resource pack delivery to avoid flooding connections.
 */
object ServerPacketHandler {

    /** Pending resource pack transfers: player UUID → next chunk index to send. */
    private val pendingTransfers = mutableMapOf<UUID, Int>()

    /** How many 512KB chunks to send per server tick (512KB × 2 = 1MB/tick = ~20MB/sec). */
    private const val CHUNKS_PER_TICK = 2

    fun register() {
        // Send skin list + pack hash when player joins
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val player = handler.player
            val packHash = ResourcePackTransfer.packHash
            CobblemonSkinMod.LOGGER.info("Sending skin list to ${player.name.string} (${SkinManager.getSkinInfoList().size} skins, pack=$packHash)")
            SkinPackets.sendSkinList(player, SkinManager.getSkinInfoList(), packHash)
        }

        // Tick-based throttled chunk sending
        ServerTickEvents.END_SERVER_TICK.register { server ->
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
                        ServerPlayNetworking.send(player, ResourcePackChunkS2C(i, totalChunks, ResourcePackTransfer.getChunkData(i)))
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
