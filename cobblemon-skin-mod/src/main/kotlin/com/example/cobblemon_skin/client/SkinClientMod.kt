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
import org.lwjgl.glfw.GLFW

@Environment(EnvType.CLIENT)
object SkinClientMod : ClientModInitializer {

    private lateinit var openGuiKey: KeyMapping

    override fun onInitializeClient() {
        // Register keybinding: K key
        openGuiKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "key.cobblemon_skin.open_gui",
                GLFW.GLFW_KEY_K,
                "key.categories.cobblemon_skin"
            )
        )

        // Open SkinScreen each time the key is pressed
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (openGuiKey.consumeClick()) {
                val skins = ClientSkinCache.getAvailableSkinIds()
                if (skins.isNotEmpty()) {
                    client.setScreen(SkinScreen(skins))
                }
            }
        }

        // ── S2C Packet Handlers ─────────────────────────────────────────────

        // Receive skin list from server on join
        ClientPlayNetworking.registerGlobalReceiver(SkinListPayload.ID) { payload: SkinListPayload, context ->
            context.client().execute {
                ClientSkinCache.updateSkinList(payload.skins)
            }
        }

        // Receive skin resource data (on-demand response)
        ClientPlayNetworking.registerGlobalReceiver(SkinResourcePayload.ID) { payload: SkinResourcePayload, context ->
            context.client().execute {
                ClientSkinCache.onSkinResourceReceived(payload.data)
                // TODO: trigger resource reload if needed
            }
        }

        // Receive skin apply broadcast from server
        ClientPlayNetworking.registerGlobalReceiver(SkinApplyBroadcast.ID) { payload: SkinApplyBroadcast, context ->
            context.client().execute {
                CobblemonSkinMod.LOGGER.info("Skin apply broadcast: player=${payload.playerUUID} slot=${payload.slot} skin=${payload.skinId}")
                // The Cobblemon aspect system handles visual updates automatically
            }
        }
    }

    /** Request skin resource data from server (called when user previews a skin). */
    fun requestSkinResource(skinId: String) {
        if (ClientSkinCache.isSkinDownloaded(skinId) || ClientSkinCache.isRequestPending(skinId)) return
        ClientSkinCache.markRequestPending(skinId)
        try {
            ClientPlayNetworking.send(SkinResourceRequest(skinId))
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.debug("Failed to request skin resource: ${e.message}")
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
