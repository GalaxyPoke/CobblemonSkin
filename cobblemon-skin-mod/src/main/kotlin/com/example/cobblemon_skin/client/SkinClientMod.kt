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

/**
 * Client-side mod initializer.
 * Receives skin list + resource pack chunks from server (sent by Bukkit plugin).
 * Apply/Clear uses chat commands for maximum Arclight compatibility.
 */
@Environment(EnvType.CLIENT)
object SkinClientMod : ClientModInitializer {

    private lateinit var openGuiKey: KeyMapping

    /** The resolver pack hash received from the server. */
    private var serverPackHash: String = ""

    /** Countdown ticks before requesting the resolver pack. */
    private var packRequestDelay = -1

    override fun onInitializeClient() {
        // Register keybinding: K key
        openGuiKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "key.cobblemon_skin.open_gui",
                GLFW.GLFW_KEY_K,
                "key.categories.cobblemon_skin"
            )
        )

        // Load local fallback skins (from skin_list.json if present)
        ClientSkinCache.loadFromLocal()

        // Tick handler: keybinding + delayed pack request + reload
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (openGuiKey.consumeClick()) {
                // 按 K 时轻量刷新皮肤列表（只重新读取 skin_list.json，不重载资源包）
                ClientSkinCache.refreshSkinList()
                val skins = ClientSkinCache.getAvailableSkinIds()
                if (skins.isNotEmpty()) {
                    client.setScreen(SkinScreen(skins))
                }
            }

            // Delayed resolver pack request
            if (packRequestDelay > 0) {
                packRequestDelay--
            } else if (packRequestDelay == 0) {
                packRequestDelay = -1
                CobblemonSkinMod.LOGGER.info("Requesting resolver pack download from server...")
                try {
                    ClientPlayNetworking.send(ResourcePackRequestC2S())
                } catch (e: Exception) {
                    CobblemonSkinMod.LOGGER.error("Failed to request resolver pack: ${e.message}")
                }
            }

            // Trigger resource reload if resolver pack or skin files were just saved
            if (ClientSkinCache.needsReload) {
                ClientSkinCache.needsReload = false
                CobblemonSkinMod.LOGGER.info("Triggering resource pack reload...")
                client.reloadResourcePacks().thenRun {
                    CobblemonSkinMod.LOGGER.info("Resource pack reload complete")
                }
            }
        }

        // ── S2C Packet Handlers (receive data from server/plugin) ───────────

        // Receive skin list + resolver pack hash from server
        ClientPlayNetworking.registerGlobalReceiver(SkinListPayload.ID) { payload: SkinListPayload, context ->
            context.client().execute {
                ClientSkinCache.updateSkinList(payload.skins)
                serverPackHash = payload.packHash

                if (payload.packHash.isNotEmpty() && !ClientSkinCache.isResolverPackCached(payload.packHash)) {
                    CobblemonSkinMod.LOGGER.info("Resolver pack hash mismatch, will request download in 3 seconds...")
                    packRequestDelay = 60 // 60 ticks = 3 seconds
                } else {
                    CobblemonSkinMod.LOGGER.info("Resolver pack is up-to-date, no download needed")
                }
            }
        }

        // Receive resolver pack chunk — stream to disk
        ClientPlayNetworking.registerGlobalReceiver(ResourcePackChunkS2C.ID) { payload: ResourcePackChunkS2C, context ->
            context.client().execute {
                ClientSkinCache.onChunkReceived(payload.chunkIndex, payload.totalChunks, payload.data, serverPackHash)
            }
        }

        // Receive per-skin resource files (on-demand)
        ClientPlayNetworking.registerGlobalReceiver(SkinResourcePayload.ID) { payload: SkinResourcePayload, context ->
            context.client().execute {
                val skinId = payload.data.skinId
                val files = payload.data.files
                CobblemonSkinMod.LOGGER.info("Received ${files.size} files for skin '$skinId'")
                Thread({
                    ClientSkinCache.saveSkinFiles(skinId, files)
                }, "CobblemonSkin-SaveSkin").start()
            }
        }

        // Receive skin apply broadcast
        ClientPlayNetworking.registerGlobalReceiver(SkinApplyBroadcast.ID) { payload: SkinApplyBroadcast, context ->
            context.client().execute {
                CobblemonSkinMod.LOGGER.info("Skin apply broadcast: player=${payload.playerUUID} slot=${payload.slot} skin=${payload.skinId}")
                // If this skin's resources aren't cached, request them
                if (payload.skinId.isNotEmpty() && !ClientSkinCache.isSkinResourceCached(payload.skinId)) {
                    requestSkinResources(payload.skinId)
                }
            }
        }
    }

    /**
     * Requests a specific skin's model/texture files from the server.
     * Called when a skin is selected in the GUI or when encountering a skinned Pokemon.
     */
    fun requestSkinResources(skinId: String) {
        if (ClientSkinCache.isSkinResourceCached(skinId)) {
            ClientSkinCache.touchSkin(skinId)
            return
        }
        if (skinId in ClientSkinCache.pendingDownloads) return

        ClientSkinCache.pendingDownloads.add(skinId)
        CobblemonSkinMod.LOGGER.info("Requesting skin resources for '$skinId'...")
        try {
            ClientPlayNetworking.send(SkinResourceRequest(skinId))
        } catch (e: Exception) {
            ClientSkinCache.pendingDownloads.remove(skinId)
            CobblemonSkinMod.LOGGER.error("Failed to request skin resources: ${e.message}")
        }
    }

    /** Apply skin via chat command (reliable on Arclight). */
    fun requestApplySkin(slot: Int, skinId: String) {
        val conn = Minecraft.getInstance().player?.connection ?: return
        conn.sendCommand("pokemonskin set $skinId $slot")
    }

    /** Clear skin via chat command. */
    fun requestClearSkin(slot: Int) {
        val conn = Minecraft.getInstance().player?.connection ?: return
        conn.sendCommand("pokemonskin clear $slot")
    }
}
