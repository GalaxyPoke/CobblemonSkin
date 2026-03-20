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
    private var serverResolverHash: String = ""

    /** The asset pack hash received from the server. */
    private var serverAssetHash: String = ""

    /** Countdown ticks before requesting packs. */
    private var resolverRequestDelay = -1
    private var assetRequestDelay = -1

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

        // Tick handler: keybinding + delayed pack requests + reload
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (openGuiKey.consumeClick()) {
                ClientSkinCache.refreshSkinList()
                val skins = ClientSkinCache.getAvailableSkinIds()
                if (skins.isEmpty()) continue

                if (!ClientSkinCache.assetsFullyLoaded) {
                    // 皮肤资源尚未加载完成，显示进度提示
                    val pct = ClientSkinCache.assetDownloadProgress
                    val player = client.player ?: continue
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§e皮肤资源加载中... ($pct%)"),
                        true
                    )
                } else {
                    client.setScreen(SkinScreen(skins))
                }
            }

            // Delayed resolver pack request
            if (resolverRequestDelay > 0) {
                resolverRequestDelay--
            } else if (resolverRequestDelay == 0) {
                resolverRequestDelay = -1
                CobblemonSkinMod.LOGGER.info("Requesting resolver pack from server...")
                try {
                    ClientPlayNetworking.send(ResourcePackRequestC2S(0))
                } catch (e: Exception) {
                    CobblemonSkinMod.LOGGER.error("Failed to request resolver pack: ${e.message}")
                }
            }

            // Delayed asset pack request
            if (assetRequestDelay > 0) {
                assetRequestDelay--
            } else if (assetRequestDelay == 0) {
                assetRequestDelay = -1
                CobblemonSkinMod.LOGGER.info("Requesting asset pack from server...")
                try {
                    ClientPlayNetworking.send(ResourcePackRequestC2S(1))
                } catch (e: Exception) {
                    CobblemonSkinMod.LOGGER.error("Failed to request asset pack: ${e.message}")
                }
            }

            // After resolver pack extracted (no reload!), immediately request asset pack
            if (ClientSkinCache.resolverReady && assetRequestDelay < 0 &&
                serverAssetHash.isNotEmpty() && !ClientSkinCache.isAssetPackCached(serverAssetHash)) {
                CobblemonSkinMod.LOGGER.info("Resolver ready, requesting asset pack in 1 second...")
                assetRequestDelay = 20 // 20 ticks = 1 second
            }

            // Trigger resource reload only after ASSET pack is extracted (models+resolvers both present)
            if (ClientSkinCache.needsReload) {
                ClientSkinCache.needsReload = false
                CobblemonSkinMod.LOGGER.info("Asset pack ready, triggering resource pack reload...")
                client.reloadResourcePacks().thenRun {
                    CobblemonSkinMod.LOGGER.info("Resource pack reload complete — all skins available")
                }
            }
        }

        // ── S2C Packet Handlers (receive data from server/plugin) ───────────

        // Receive skin list + resolver/asset pack hashes from server
        ClientPlayNetworking.registerGlobalReceiver(SkinListPayload.ID) { payload: SkinListPayload, context ->
            context.client().execute {
                ClientSkinCache.updateSkinList(payload.skins)
                serverResolverHash = payload.packHash
                serverAssetHash = payload.assetHash

                // Check if resolver pack needs download
                if (payload.packHash.isNotEmpty() && !ClientSkinCache.isResolverPackCached(payload.packHash)) {
                    CobblemonSkinMod.LOGGER.info("Resolver pack hash mismatch, will request in 3 seconds...")
                    resolverRequestDelay = 60
                } else {
                    CobblemonSkinMod.LOGGER.info("Resolver pack is up-to-date")
                    // Resolver cached, check if asset pack needs download
                    if (payload.assetHash.isNotEmpty() && !ClientSkinCache.isAssetPackCached(payload.assetHash)) {
                        CobblemonSkinMod.LOGGER.info("Asset pack hash mismatch, will request in 5 seconds...")
                        assetRequestDelay = 100
                    } else {
                        CobblemonSkinMod.LOGGER.info("Asset pack is up-to-date")
                    }
                }
            }
        }

        // Receive pack chunks — route by packType (0=resolver, 1=asset)
        ClientPlayNetworking.registerGlobalReceiver(ResourcePackChunkS2C.ID) { payload: ResourcePackChunkS2C, context ->
            context.client().execute {
                val hash = if (payload.packType == 0) serverResolverHash else serverAssetHash
                ClientSkinCache.onChunkReceived(payload.packType, payload.chunkIndex, payload.totalChunks, payload.data, hash)
            }
        }

        // Receive per-skin resource files (on-demand, kept for individual requests)
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
            }
        }
    }

    /**
     * Requests a specific skin's model/texture files from the server (on-demand fallback).
     */
    fun requestSkinResources(skinId: String) {
        if (ClientSkinCache.isSkinResourceCached(skinId)) {
            ClientSkinCache.touchSkin(skinId)
            return
        }
        if (ClientSkinCache.assetsFullyLoaded) return
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
