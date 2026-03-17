package com.example.cobblemon_skin.client

import com.example.cobblemon_skin.CobblemonSkinMod
import com.example.cobblemon_skin.network.SkinListPayload
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

    /** Skin IDs synced from the server on login. */
    val clientSkins = mutableListOf<String>()

    private lateinit var openGuiKey: KeyMapping

    override fun onInitializeClient() {
        // Populate from local config (loaded during onInitialize); server packet may update later
        clientSkins.addAll(CobblemonSkinMod.registeredSkins)

        // Register keybinding: K key  (category shown in Controls settings)
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
                client.setScreen(SkinScreen(clientSkins.toList()))
            }
        }

        // Receive skin list packet from server
        ClientPlayNetworking.registerGlobalReceiver(SkinListPayload.ID) { payload: SkinListPayload, context ->
            context.client().execute {
                clientSkins.clear()
                clientSkins.addAll(payload.skins)
            }
        }
    }
}
