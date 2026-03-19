package com.example.cobblemon_skin.client

import com.example.cobblemon_skin.CobblemonSkinMod
import com.example.cobblemon_skin.network.SkinInfo
import com.example.cobblemon_skin.network.SkinResourceData
import java.io.File

/**
 * Client-side skin cache. Stores skin metadata received from server
 * and caches downloaded resource data to disk.
 */
object ClientSkinCache {

    /** Skin metadata list received from server on join. */
    var skinList: List<SkinInfo> = emptyList()
        private set

    /** Set of skinIds whose resources have been downloaded and written to cache. */
    private val downloadedSkins = mutableSetOf<String>()

    /** Pending resource requests (skinIds currently being downloaded). */
    private val pendingRequests = mutableSetOf<String>()

    private val cacheDir: File by lazy {
        val gameDir = net.fabricmc.loader.api.FabricLoader.getInstance().gameDir.toFile()
        File(gameDir, "resourcepacks/cobblemon_skin_cache")
    }

    fun updateSkinList(skins: List<SkinInfo>) {
        skinList = skins
        // Update CobblemonSkinMod registrations
        skins.forEach { info ->
            CobblemonSkinMod.registerSkin(info.skinId)
            if (info.species.isNotEmpty()) {
                CobblemonSkinMod.skinSpeciesMap[info.skinId] = info.species
            }
            CobblemonSkinMod.skinMetaMap[info.skinId] = CobblemonSkinMod.SkinMeta(
                description = info.description,
                quality = info.quality
            )
        }
        CobblemonSkinMod.LOGGER.info("Client received skin list: ${skins.size} skins")
    }

    fun getAvailableSkinIds(): List<String> = skinList.map { it.skinId }

    fun isSkinDownloaded(skinId: String): Boolean = skinId in downloadedSkins

    fun isRequestPending(skinId: String): Boolean = skinId in pendingRequests

    fun markRequestPending(skinId: String) { pendingRequests.add(skinId) }

    /**
     * Called when skin resource data is received from server.
     * Writes files to the cache resource pack directory.
     */
    fun onSkinResourceReceived(data: SkinResourceData) {
        pendingRequests.remove(data.skinId)
        val skinId = data.skinId

        try {
            // Ensure cache pack exists with mcmeta
            cacheDir.mkdirs()
            val mcmeta = File(cacheDir, "pack.mcmeta")
            if (!mcmeta.exists()) {
                mcmeta.writeText("""{"pack":{"pack_format":34,"description":"CobblemonSkin client cache"}}""")
            }

            val assetsDir = File(cacheDir, "assets/cobblemon")

            // Write resolver
            val resolverDir = File(assetsDir, "bedrock/pokemon/resolvers/skin_cache")
            resolverDir.mkdirs()
            File(resolverDir, "${skinId}.json").writeBytes(data.resolverJson)

            // Write model
            if (data.modelGeo != null) {
                val modelsDir = File(assetsDir, "bedrock/pokemon/models")
                modelsDir.mkdirs()
                // Extract model filename from resolver JSON
                val modelName = extractModelName(String(data.resolverJson))
                if (modelName != null) {
                    File(modelsDir, "$modelName.json").writeBytes(data.modelGeo)
                }
            }

            // Write poser
            if (data.poserJson != null) {
                val posersDir = File(assetsDir, "bedrock/pokemon/posers")
                posersDir.mkdirs()
                val poserName = extractPoserName(String(data.resolverJson))
                if (poserName != null) {
                    File(posersDir, "$poserName.json").writeBytes(data.poserJson)
                }
            }

            // Write animation
            if (data.animationJson != null) {
                val animDir = File(assetsDir, "bedrock/pokemon/animations")
                animDir.mkdirs()
                // Use skinId as animation group name
                File(animDir, "$skinId.animation.json").writeBytes(data.animationJson)
            }

            // Write textures (main + extras)
            for ((path, bytes) in data.extraTextures) {
                val texFile = File(assetsDir, path.removePrefix("cobblemon/"))
                texFile.parentFile.mkdirs()
                texFile.writeBytes(bytes)
            }

            downloadedSkins.add(skinId)
            CobblemonSkinMod.LOGGER.info("Cached skin resource: $skinId")

        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("Failed to cache skin resource $skinId: ${e.message}")
        }
    }

    /**
     * Check if a resource reload is needed (new skins downloaded since last reload).
     */
    private var lastReloadCount = 0
    fun needsResourceReload(): Boolean = downloadedSkins.size > lastReloadCount
    fun markReloaded() { lastReloadCount = downloadedSkins.size }

    fun clear() {
        skinList = emptyList()
        downloadedSkins.clear()
        pendingRequests.clear()
        lastReloadCount = 0
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun extractModelName(resolverJson: String): String? {
        val regex = """"model"\s*:\s*"cobblemon:([^"]+)"""".toRegex()
        val match = regex.find(resolverJson) ?: return null
        val ref = match.groupValues[1]
        return if (ref.endsWith(".geo")) ref else if (ref.endsWith(".geo.json")) ref.removeSuffix(".json") else "$ref.geo"
    }

    private fun extractPoserName(resolverJson: String): String? {
        val regex = """"poser"\s*:\s*"cobblemon:([^"]+)"""".toRegex()
        val match = regex.find(resolverJson) ?: return null
        return match.groupValues[1]
    }
}
