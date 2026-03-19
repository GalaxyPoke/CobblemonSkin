package com.example.cobblemon_skin.client

import com.example.cobblemon_skin.CobblemonSkinMod
import com.example.cobblemon_skin.loader.SkinPackLoader
import com.example.cobblemon_skin.network.SkinInfo
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

/**
 * Client-side skin cache. Stores skin metadata received from server,
 * manages resource pack downloading via chunked transfer, and handles
 * ZIP extraction + resource reload.
 */
object ClientSkinCache {

    /** Skin metadata list received from server on join. */
    var skinList: List<SkinInfo> = emptyList()
        private set

    /** The resource pack directory where server-delivered packs are extracted. */
    private val packDir: File by lazy {
        FabricLoader.getInstance().gameDir.resolve("resourcepacks/cobblemon_skin_skins").toFile()
    }

    /** File storing the hash of the currently cached resource pack. */
    private val hashFile: File by lazy {
        File(packDir, ".server_pack_hash")
    }

    /** Temp file for streaming resource pack chunks to disk (avoids memory pressure). */
    private var tempFile: File? = null
    private var tempOutputStream: FileOutputStream? = null
    private var expectedChunks = 0
    private var receivedChunks = 0

    /** Flag indicating a resource reload should be triggered on next tick. */
    var needsReload = false

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

    /**
     * Check if the locally cached resource pack matches the server's hash.
     * Returns true if the pack is up-to-date and no download is needed.
     */
    fun isPackCached(serverHash: String): Boolean {
        if (serverHash.isEmpty()) return true
        if (!hashFile.exists()) return false
        return try {
            hashFile.readText().trim() == serverHash
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Called when a resource pack chunk is received from the server.
     * Accumulates chunks and triggers extraction when all are received.
     */
    fun onChunkReceived(chunkIndex: Int, totalChunks: Int, data: ByteArray, packHash: String) {
        try {
            if (tempFile == null) {
                val gameDir = FabricLoader.getInstance().gameDir.toFile()
                tempFile = File(gameDir, "cobblemon_skin_pack_download.tmp")
                tempOutputStream = FileOutputStream(tempFile!!)
                expectedChunks = totalChunks
                receivedChunks = 0
                CobblemonSkinMod.LOGGER.info("Starting resource pack download ($totalChunks chunks) → temp file")
            }

            tempOutputStream!!.write(data)
            receivedChunks++

            if (receivedChunks % 10 == 0 || receivedChunks >= expectedChunks) {
                CobblemonSkinMod.LOGGER.info("Resource pack download: $receivedChunks/$expectedChunks chunks")
            }

            if (receivedChunks >= expectedChunks) {
                tempOutputStream!!.close()
                tempOutputStream = null
                val zipFile = tempFile!!
                tempFile = null

                CobblemonSkinMod.LOGGER.info("Resource pack download complete (${zipFile.length() / 1024}KB)")
                extractServerPack(zipFile, packHash)
                zipFile.delete()
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

    /**
     * Extract the server-delivered resource pack ZIP to the resource pack directory
     * and schedule a resource reload.
     */
    private fun extractServerPack(zipFile: File, packHash: String) {
        try {
            // Clean existing pack directory (except .server_pack_hash)
            if (packDir.exists()) {
                packDir.listFiles()?.forEach { f ->
                    if (f.name != ".server_pack_hash") {
                        if (f.isDirectory) f.deleteRecursively() else f.delete()
                    }
                }
            }
            packDir.mkdirs()

            // Extract ZIP from temp file (streams from disk, low memory usage)
            var fileCount = 0
            ZipInputStream(FileInputStream(zipFile)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val file = File(packDir, entry.name)
                    // Zip slip protection
                    if (!file.canonicalPath.startsWith(packDir.canonicalPath)) {
                        CobblemonSkinMod.LOGGER.warn("Skipping zip entry outside target dir: ${entry.name}")
                        entry = zis.nextEntry
                        continue
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

            // Write hash file
            hashFile.writeText(packHash)

            // Ensure pack is enabled in options.txt
            SkinPackLoader.ensurePackEnabled()

            CobblemonSkinMod.LOGGER.info("Extracted server resource pack: $fileCount files")

            // Schedule resource reload
            needsReload = true

        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("Failed to extract server resource pack: ${e.message}")
        }
    }

    fun clear() {
        skinList = emptyList()
        cleanupTempFile()
        expectedChunks = 0
        receivedChunks = 0
        needsReload = false
    }
}
