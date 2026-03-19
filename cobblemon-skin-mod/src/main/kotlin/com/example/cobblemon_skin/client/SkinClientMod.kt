package com.example.cobblemon_skin.client

import com.example.cobblemon_skin.CobblemonSkinMod
import com.example.cobblemon_skin.network.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW

@Environment(EnvType.CLIENT)
object SkinClientMod : ClientModInitializer {

    private lateinit var openGuiKey: KeyMapping

    /** The pack hash received from the server, used to tag downloaded chunks. */
    private var serverPackHash: String = ""

    override fun onInitializeClient() {
        // Register keybinding: K key
        openGuiKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "key.cobblemon_skin.open_gui",
                GLFW.GLFW_KEY_K,
                "key.categories.cobblemon_skin"
            )
        )

        // Open SkinScreen each time the key is pressed + check for pending resource reload
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (openGuiKey.consumeClick()) {
                val skins = ClientSkinCache.getAvailableSkinIds()
                if (skins.isNotEmpty()) {
                    client.setScreen(SkinScreen(skins))
                }
            }

            // Trigger resource reload if pack was just extracted
            if (ClientSkinCache.needsReload) {
                ClientSkinCache.needsReload = false
                CobblemonSkinMod.LOGGER.info("Triggering resource pack reload...")
                client.reloadResourcePacks().thenRun {
                    CobblemonSkinMod.LOGGER.info("Resource pack reload complete")
                }
            }
        }

        // ── S2C Packet Handlers ─────────────────────────────────────────────

        // Receive skin list + pack hash from server on join
        ClientPlayNetworking.registerGlobalReceiver(SkinListPayload.ID) { payload: SkinListPayload, context ->
            context.client().execute {
                ClientSkinCache.updateSkinList(payload.skins)

                // Check if we need to download the resource pack
                serverPackHash = payload.packHash
                if (payload.packHash.isNotEmpty() && !ClientSkinCache.isPackCached(payload.packHash)) {
                    CobblemonSkinMod.LOGGER.info("Resource pack hash mismatch, requesting download...")
                    try {
                        ClientPlayNetworking.send(ResourcePackRequestC2S())
                    } catch (e: Exception) {
                        CobblemonSkinMod.LOGGER.error("Failed to request resource pack: ${e.message}")
                    }
                } else {
                    CobblemonSkinMod.LOGGER.info("Resource pack is up-to-date, no download needed")
                }
            }
        }

        // Receive resource pack chunk from server
        ClientPlayNetworking.registerGlobalReceiver(ResourcePackChunkS2C.ID) { payload: ResourcePackChunkS2C, context ->
            context.client().execute {
                ClientSkinCache.onChunkReceived(payload.chunkIndex, payload.totalChunks, payload.data, serverPackHash)
            }
        }

        // Receive skin resource data (on-demand, kept for compatibility)
        ClientPlayNetworking.registerGlobalReceiver(SkinResourcePayload.ID) { payload: SkinResourcePayload, context ->
            context.client().execute {
                CobblemonSkinMod.LOGGER.debug("Received on-demand skin resource: ${payload.data.skinId}")
            }
        }

        // Receive skin apply broadcast from server
        ClientPlayNetworking.registerGlobalReceiver(SkinApplyBroadcast.ID) { payload: SkinApplyBroadcast, context ->
            context.client().execute {
                CobblemonSkinMod.LOGGER.info("Skin apply broadcast: player=${payload.playerUUID} slot=${payload.slot} skin=${payload.skinId}")
            }
        }
    }

    /** Request server to apply skin to party slot. */
    fun requestApplySkin(slot: Int, skinId: String) {
        try {
            ClientPlayNetworking.send(SkinApplyRequest(slot, skinId))
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.debug("Failed to send skin apply request: ${e.message}")
        }
    }

    /** Request server to clear skin from party slot. */
    fun requestClearSkin(slot: Int) {
        try {
            ClientPlayNetworking.send(SkinClearRequest(slot))
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.debug("Failed to send skin clear request: ${e.message}")
        }
    }
}
