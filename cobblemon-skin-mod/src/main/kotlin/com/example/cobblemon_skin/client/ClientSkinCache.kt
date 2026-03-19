package com.example.cobblemon_skin.client

import com.example.cobblemon_skin.CobblemonSkinMod
import com.example.cobblemon_skin.network.SkinInfo
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

/**
 * Two-phase client-side skin cache:
 *
 * Phase 1 (on join): Receives resolver-only pack (~100KB) containing resolvers +
 *   pack.mcmeta + skin_list.json. After extraction, Cobblemon knows all skin
 *   variations exist (but models/textures may be missing → default appearance).
 *
 * Phase 2 (on demand): When a skin's model/textures are needed, client requests
 *   them from the server. Files are saved to cache and resource reload is triggered.
 *   LRU eviction keeps cache size bounded.
 */
object ClientSkinCache {

    /** Skin IDs available to the client. */
    private var skinIds: List<String> = emptyList()
    private var localLoaded = false

    /** Flag indicating a resource reload should be triggered on next tick. */
    var needsReload = false

    /** Max cache size for on-demand skin files (models+textures). Resolvers are exempt. */
    private const val MAX_CACHE_BYTES = 100L * 1024 * 1024  // 100MB

    private val packDir: File by lazy {
        FabricLoader.getInstance().gameDir.resolve("cobblemon_skin_cache").toFile()
    }

    private val hashFile: File by lazy { File(packDir, ".resolver_pack_hash") }
    private val lruFile: File by lazy { File(packDir, ".lru_cache.json") }

    /** LRU tracking: skinId → last access timestamp (ms). */
    private val lruMap = mutableMapOf<String, Long>()

    /** Skins currently being downloaded (avoid duplicate requests). */
    val pendingDownloads = mutableSetOf<String>()

    // ── Temp state for streaming resolver pack download ──
    private var tempFile: File? = null
    private var tempOutputStream: FileOutputStream? = null
    private var expectedChunks = 0
    private var receivedChunks = 0

    // ═══════════════════════════════════════════════════════════════════════
    //  LOCAL LOADING (skin_list.json fallback)
    // ═══════════════════════════════════════════════════════════════════════

    fun loadFromLocal() {
        if (localLoaded) return
        localLoaded = true

        loadLruMap()

        val listFile = File(packDir, "skin_list.json")
        if (!listFile.exists()) {
            CobblemonSkinMod.LOGGER.info("No skin_list.json found — waiting for server data")
            return
        }

        try {
            val root = JsonParser.parseString(listFile.readText()).asJsonObject
            val arr = root.getAsJsonArray("skins") ?: return
            val ids = mutableListOf<String>()

            for (elem in arr) {
                val obj = elem.asJsonObject
                val skinId = obj.get("skinId")?.asString ?: continue
                val species = obj.get("species")?.asString ?: ""
                val quality = obj.get("quality")?.asString ?: "普通"
                val description = obj.get("description")?.asString ?: ""
                val detail = obj.get("detail")?.asString ?: ""
                val obtain = obj.get("obtain")?.asString ?: ""

                CobblemonSkinMod.registerSkin(skinId)
                ids.add(skinId)
                if (species.isNotEmpty()) {
                    CobblemonSkinMod.skinSpeciesMap[skinId] = species
                }
                CobblemonSkinMod.skinMetaMap[skinId] = CobblemonSkinMod.SkinMeta(description, quality, obtain, detail)

                val uiScale = obj.get("uiScale")?.asFloat
                val uiOx = obj.get("uiOffsetX")?.asInt
                val uiOy = obj.get("uiOffsetY")?.asInt
                if (uiScale != null || uiOx != null || uiOy != null) {
                    CobblemonSkinMod.skinUiConfigs[skinId] = CobblemonSkinMod.SkinUiConfig(
                        uiScale ?: 1.0f, uiOx ?: 0, uiOy ?: 0
                    )
                }
            }
            skinIds = ids
            CobblemonSkinMod.LOGGER.info("Loaded ${ids.size} skins from local skin_list.json")
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("Failed to read skin_list.json: ${e.message}")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  NETWORK: skin list from server
    // ═══════════════════════════════════════════════════════════════════════

    fun updateSkinList(skins: List<SkinInfo>) {
        val ids = mutableListOf<String>()
        skins.forEach { info ->
            CobblemonSkinMod.registerSkin(info.skinId)
            ids.add(info.skinId)
            if (info.species.isNotEmpty()) {
                CobblemonSkinMod.skinSpeciesMap[info.skinId] = info.species
            }
            CobblemonSkinMod.skinMetaMap[info.skinId] = CobblemonSkinMod.SkinMeta(
                description = info.description, quality = info.quality
            )
        }
        skinIds = ids
        CobblemonSkinMod.LOGGER.info("Client received skin list from server: ${skins.size} skins")
    }

    fun getAvailableSkinIds(): List<String> = skinIds

    /**
     * 轻量刷新：如果皮肤列表为空，重新读取本地 skin_list.json。
     */
    fun refreshSkinList() {
        if (skinIds.isEmpty()) {
            localLoaded = false
            loadFromLocal()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PHASE 1: Resolver pack streaming (on join)
    // ═══════════════════════════════════════════════════════════════════════

    fun isResolverPackCached(serverHash: String): Boolean {
        if (serverHash.isEmpty()) return true
        if (!hashFile.exists()) return false
        return try { hashFile.readText().trim() == serverHash } catch (_: Exception) { false }
    }

    fun onChunkReceived(chunkIndex: Int, totalChunks: Int, data: ByteArray, packHash: String) {
        try {
            if (tempFile == null) {
                val gameDir = FabricLoader.getInstance().gameDir.toFile()
                tempFile = File(gameDir, "cobblemon_skin_resolvers_download.tmp")
                tempOutputStream = FileOutputStream(tempFile!!)
                expectedChunks = totalChunks
                receivedChunks = 0
                CobblemonSkinMod.LOGGER.info("Starting resolver pack download ($totalChunks chunks)")
            }

            tempOutputStream!!.write(data)
            receivedChunks++

            if (receivedChunks >= expectedChunks) {
                tempOutputStream!!.close()
                tempOutputStream = null
                val zipFile = tempFile!!
                tempFile = null

                CobblemonSkinMod.LOGGER.info("Resolver pack download complete (${zipFile.length() / 1024}KB)")

                Thread({
                    extractResolverPack(zipFile, packHash)
                    zipFile.delete()
                }, "CobblemonSkin-Extract").start()
            }
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("Error receiving resolver chunk: ${e.message}")
            cleanupTempFile()
        }
    }

    /**
     * Extracts resolver-only pack. Preserves existing model/texture files
     * (only overwrites resolvers, pack.mcmeta, skin_list.json).
     */
    private fun extractResolverPack(zipFile: File, packHash: String) {
        try {
            packDir.mkdirs()

            var fileCount = 0
            ZipInputStream(FileInputStream(zipFile)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val file = File(packDir, entry.name)
                    if (!file.canonicalPath.startsWith(packDir.canonicalPath)) {
                        entry = zis.nextEntry; continue
                    }
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile.mkdirs()
                        file.outputStream().use { zis.copyTo(it) }
                        fileCount++
                    }
                    entry = zis.nextEntry
                }
            }

            hashFile.writeText(packHash)
            CobblemonSkinMod.LOGGER.info("Extracted resolver pack: $fileCount files")
            needsReload = true
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("Failed to extract resolver pack: ${e.message}")
        }
    }

    private fun cleanupTempFile() {
        try { tempOutputStream?.close() } catch (_: Exception) {}
        tempOutputStream = null
        try { tempFile?.delete() } catch (_: Exception) {}
        tempFile = null
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PHASE 2: Per-skin on-demand file caching
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Checks if a skin's model/texture files are locally cached.
     * Looks for the texture directory assets/cobblemon/textures/pokemon/<skinId>/.
     */
    fun isSkinResourceCached(skinId: String): Boolean {
        val texDir = File(packDir, "assets/cobblemon/textures/pokemon/$skinId")
        return texDir.exists() && (texDir.listFiles()?.isNotEmpty() == true)
    }

    /**
     * Saves per-skin resource files (model, textures, posers, animations) to cache.
     * Triggers resource reload after saving.
     */
    fun saveSkinFiles(skinId: String, files: Map<String, ByteArray>) {
        try {
            var saved = 0
            for ((relativePath, content) in files) {
                val dest = File(packDir, relativePath)
                dest.parentFile.mkdirs()
                dest.writeBytes(content)
                saved++
            }

            // Update LRU
            lruMap[skinId] = System.currentTimeMillis()
            saveLruMap()

            // Enforce cache size limit
            enforceMaxCacheSize()

            pendingDownloads.remove(skinId)
            CobblemonSkinMod.LOGGER.info("Saved $saved files for skin '$skinId', triggering reload...")
            needsReload = true
        } catch (e: Exception) {
            pendingDownloads.remove(skinId)
            CobblemonSkinMod.LOGGER.error("Failed to save skin files for '$skinId': ${e.message}")
        }
    }

    /**
     * Marks a skin as recently accessed (for LRU).
     */
    fun touchSkin(skinId: String) {
        if (isSkinResourceCached(skinId)) {
            lruMap[skinId] = System.currentTimeMillis()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  LRU CACHE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════

    private fun loadLruMap() {
        try {
            if (!lruFile.exists()) return
            val root = JsonParser.parseString(lruFile.readText()).asJsonObject
            for ((key, value) in root.entrySet()) {
                lruMap[key] = value.asLong
            }
        } catch (_: Exception) {}
    }

    private fun saveLruMap() {
        try {
            val obj = com.google.gson.JsonObject()
            for ((k, v) in lruMap) obj.addProperty(k, v)
            lruFile.writeText(com.google.gson.GsonBuilder().create().toJson(obj))
        } catch (_: Exception) {}
    }

    /**
     * Evicts oldest accessed skins' model/texture files until cache is under limit.
     * Resolver files are NEVER evicted (they're tiny and needed for Cobblemon to see skins).
     */
    private fun enforceMaxCacheSize() {
        try {
            val texturesRoot = File(packDir, "assets/cobblemon/textures/pokemon")
            val modelsRoot = File(packDir, "assets/cobblemon/bedrock/pokemon/models")
            if (!texturesRoot.exists()) return

            // Calculate current cache size (textures + models only)
            var totalSize = 0L
            texturesRoot.walkTopDown().filter { it.isFile }.forEach { totalSize += it.length() }
            if (modelsRoot.exists()) {
                modelsRoot.walkTopDown().filter { it.isFile }.forEach { totalSize += it.length() }
            }

            if (totalSize <= MAX_CACHE_BYTES) return

            // Sort by access time, evict oldest first
            val sorted = lruMap.entries.sortedBy { it.value }
            for ((skinId, _) in sorted) {
                if (totalSize <= MAX_CACHE_BYTES * 0.8) break  // Evict until 80% of limit

                // Delete texture directory for this skin
                val texDir = File(texturesRoot, skinId)
                if (texDir.exists()) {
                    val freed = texDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
                    texDir.deleteRecursively()
                    totalSize -= freed
                    lruMap.remove(skinId)
                    CobblemonSkinMod.LOGGER.info("LRU evicted skin '$skinId' (freed ${freed / 1024}KB)")
                }
            }
            saveLruMap()
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.warn("LRU cache cleanup failed: ${e.message}")
        }
    }
}
