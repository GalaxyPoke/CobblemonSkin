package com.example.cobblemon_skin.server

import com.example.cobblemon_skin.CobblemonSkinMod
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Handles ZIP generation of the server's resource pack and provides
 * streaming chunk reads from disk for packet-based delivery.
 *
 * Flow:
 *   SkinPackLoader generates pack dir → this object ZIPs it to disk
 *   → computes SHA-256 hash → ServerPacketHandler sends chunks on request
 *   → Client streams chunks to disk, extracts, and reloads resources
 */
object ResourcePackTransfer {

    /** SHA-256 hash of the pack ZIP for client-side caching. */
    var packHash: String = ""
        private set

    /** Chunk size: 256KB per packet. */
    const val CHUNK_SIZE = 256 * 1024

    private var packFile: File? = null
    private var packFileSize: Long = 0

    private val generatedPackDir: File by lazy {
        FabricLoader.getInstance().gameDir.resolve("resourcepacks/cobblemon_skin_skins").toFile()
    }

    /**
     * Called after SkinPackLoader.loadAll() to ZIP the generated resource pack
     * and compute its hash for client caching.
     */
    fun prepare() {
        if (!generatedPackDir.exists() || generatedPackDir.listFiles().isNullOrEmpty()) {
            packHash = ""
            CobblemonSkinMod.LOGGER.info("ResourcePackTransfer: no resource pack to serve")
            return
        }

        try {
            val gameDir = FabricLoader.getInstance().gameDir.toFile()
            val zipFile = File(gameDir, "cobblemon_skin_pack.zip")
            FileOutputStream(zipFile).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    generatedPackDir.walkTopDown().filter { it.isFile }.forEach { file ->
                        val entryName = file.relativeTo(generatedPackDir).path.replace('\\', '/')
                        zos.putNextEntry(ZipEntry(entryName))
                        file.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
            packFile = zipFile
            packFileSize = zipFile.length()

            // Compute SHA-256 hash (streaming from disk)
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(zipFile).use { fis ->
                val buf = ByteArray(8192)
                var n: Int
                while (fis.read(buf).also { n = it } != -1) {
                    digest.update(buf, 0, n)
                }
            }
            packHash = digest.digest().joinToString("") { "%02x".format(it) }

            val sizeMB = packFileSize / (1024.0 * 1024.0)
            CobblemonSkinMod.LOGGER.info(
                "ResourcePackTransfer: prepared ${String.format("%.1f", sizeMB)}MB ZIP (hash=$packHash)"
            )
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("ResourcePackTransfer: failed to prepare: ${e.message}", e)
            packHash = ""
        }
    }

    fun getTotalChunks(): Int {
        if (packFileSize <= 0) return 0
        return ((packFileSize + CHUNK_SIZE - 1) / CHUNK_SIZE).toInt()
    }

    /**
     * Read a single chunk from the ZIP file on disk.
     * Does NOT hold the whole file in memory.
     */
    fun readChunk(chunkIndex: Int): ByteArray {
        val file = packFile ?: return ByteArray(0)
        val offset = chunkIndex.toLong() * CHUNK_SIZE
        val length = minOf(CHUNK_SIZE.toLong(), packFileSize - offset).toInt()
        if (length <= 0) return ByteArray(0)

        RandomAccessFile(file, "r").use { raf ->
            raf.seek(offset)
            val buf = ByteArray(length)
            raf.readFully(buf)
            return buf
        }
    }

    fun isReady(): Boolean = packFile != null && packHash.isNotEmpty()
}
