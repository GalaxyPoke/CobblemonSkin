package com.example.cobblemon_skin.client

import com.example.cobblemon_skin.CobblemonSkinMod
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.PackLocationInfo
import net.minecraft.server.packs.PackSelectionConfig
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.PathPackResources
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import net.minecraft.server.packs.repository.RepositorySource
import java.io.File
import java.util.*
import java.util.function.Consumer

/**
 * 动态皮肤资源包提供器。
 * 从 .minecraft/cobblemon_skin_cache/ 目录读取缓存的皮肤文件，
 * 注册为一个始终启用的内置资源包，对玩家完全透明。
 */
object DynamicSkinPackSource : RepositorySource {

    val cacheDir: File by lazy {
        FabricLoader.getInstance().gameDir.resolve("cobblemon_skin_cache").toFile()
    }

    override fun loadPacks(consumer: Consumer<Pack>) {
        if (!cacheDir.exists()) return
        val mcmeta = File(cacheDir, "pack.mcmeta")
        if (!mcmeta.exists()) return
        // Only provide this pack when asset files are present (not just resolvers).
        // Loading resolvers without models causes Cobblemon to crash.
        val assetHash = File(cacheDir, ".asset_pack_hash")
        if (!assetHash.exists()) return

        try {
            val locationInfo = PackLocationInfo(
                "cobblemon_skin_cache",
                Component.literal("CobblemonSkin Skins"),
                PackSource.BUILT_IN,
                Optional.empty()
            )

            val supplier = object : Pack.ResourcesSupplier {
                override fun openPrimary(info: PackLocationInfo) =
                    PathPackResources(info, cacheDir.toPath())
                override fun openFull(info: PackLocationInfo, metadata: Pack.Metadata) =
                    PathPackResources(info, cacheDir.toPath())
            }

            val pack = Pack.readMetaAndCreate(
                locationInfo,
                supplier,
                PackType.CLIENT_RESOURCES,
                PackSelectionConfig(true, Pack.Position.TOP, false)
            )

            if (pack != null) {
                consumer.accept(pack)
            }
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("Failed to load dynamic skin pack: ${e.message}")
        }
    }
}
