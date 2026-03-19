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
 * Client-side skin cache. Two sources:
 * 1. Local skin_list.json (immediate, available at startup)
 * 2. Server packets (SkinListPayload + ResourcePackChunkS2C, sent by Bukkit plugin)
 *
 * When server data arrives, it overrides the local data.
 */
object ClientSkinCache {

    /** Skin IDs available to the client. */
    private var skinIds: List<String> = emptyList()
    private var localLoaded = false

    /** Flag indicating a resource reload should be triggered on next tick. */
    var needsReload = false

    private val packDir: File by lazy {
        FabricLoader.getInstance().gameDir.resolve("cobblemon_skin_cache").toFile()
    }

    private val hashFile: File by lazy { File(packDir, ".server_pack_hash") }

    // ── Temp state for streaming resource pack download ──
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
     * 如果服务器已发送数据则跳过（服务器数据更新）。
     */
    fun refreshSkinList() {
        if (skinIds.isEmpty()) {
            localLoaded = false
            loadFromLocal()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  NETWORK: resource pack caching + chunk streaming
    // ═══════════════════════════════════════════════════════════════════════

    fun isPackCached(serverHash: String): Boolean {
        if (serverHash.isEmpty()) return true
        if (!hashFile.exists()) return false
        return try { hashFile.readText().trim() == serverHash } catch (_: Exception) { false }
    }

    fun onChunkReceived(chunkIndex: Int, totalChunks: Int, data: ByteArray, packHash: String) {
        try {
            if (tempFile == null) {
                val gameDir = FabricLoader.getInstance().gameDir.toFile()
                tempFile = File(gameDir, "cobblemon_skin_pack_download.tmp")
                tempOutputStream = FileOutputStream(tempFile!!)
                expectedChunks = totalChunks
                receivedChunks = 0
                CobblemonSkinMod.LOGGER.info("Starting resource pack download ($totalChunks chunks)")
            }

            tempOutputStream!!.write(data)
            receivedChunks++

            if (receivedChunks % 20 == 0 || receivedChunks >= expectedChunks) {
                val pct = (receivedChunks * 100) / expectedChunks
                CobblemonSkinMod.LOGGER.info("Resource pack download: $receivedChunks/$expectedChunks ($pct%)")
            }

            if (receivedChunks >= expectedChunks) {
                tempOutputStream!!.close()
                tempOutputStream = null
                val zipFile = tempFile!!
                tempFile = null

                CobblemonSkinMod.LOGGER.info("Resource pack download complete (${zipFile.length() / 1024}KB)")

                Thread({
                    extractServerPack(zipFile, packHash)
                    zipFile.delete()
                }, "CobblemonSkin-Extract").start()
            }
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("Error receiving chunk: ${e.message}")
            cleanupTempFile()
        }
    }

    private fun cleanupTempFile() {
        try { tempOutputStream?.close() } catch (_: Exception) {}
        tempOutputStream = null
        try { tempFile?.delete() } catch (_: Exception) {}
        tempFile = null
    }

    private fun extractServerPack(zipFile: File, packHash: String) {
        try {
            if (packDir.exists()) {
                packDir.listFiles()?.forEach { f ->
                    if (f.name != ".server_pack_hash") {
                        if (f.isDirectory) f.deleteRecursively() else f.delete()
                    }
                }
            }
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
            CobblemonSkinMod.LOGGER.info("Extracted skin cache: $fileCount files")
            needsReload = true
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("Failed to extract server resource pack: ${e.message}")
        }
    }
}
