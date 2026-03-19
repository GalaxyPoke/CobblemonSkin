package com.example.cobblemon_skin.server

import com.example.cobblemon_skin.CobblemonSkinMod
import net.fabricmc.loader.api.FabricLoader
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Handles ZIP generation of the server's resource pack and chunked delivery to clients.
 * After SkinPackLoader generates the resource pack directory, this object ZIPs it
 * and computes a SHA-256 hash for client-side caching.
 */
object ResourcePackTransfer {

    private var packZipBytes: ByteArray = ByteArray(0)
    var packHash: String = ""
        private set

    private const val CHUNK_SIZE = 512 * 1024 // 512 KB per chunk

    private val generatedPackDir: File by lazy {
        FabricLoader.getInstance().gameDir.resolve("resourcepacks/cobblemon_skin_skins").toFile()
    }

    /**
     * Called after SkinPackLoader.loadAll() to ZIP the generated resource pack
     * and compute its hash for client caching.
     */
    fun prepare() {
        if (!generatedPackDir.exists() || generatedPackDir.listFiles().isNullOrEmpty()) {
            packZipBytes = ByteArray(0)
            packHash = ""
            CobblemonSkinMod.LOGGER.info("ResourcePackTransfer: no resource pack to serve")
            return
        }

        try {
            val baos = ByteArrayOutputStream()
            ZipOutputStream(baos).use { zos ->
                generatedPackDir.walkTopDown().filter { it.isFile }.forEach { file ->
                    val entryName = file.relativeTo(generatedPackDir).path.replace('\\', '/')
                    zos.putNextEntry(ZipEntry(entryName))
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
            packZipBytes = baos.toByteArray()

            // Compute SHA-256 hash
            val digest = MessageDigest.getInstance("SHA-256")
            packHash = digest.digest(packZipBytes).joinToString("") { "%02x".format(it) }

            val sizeMB = packZipBytes.size / (1024.0 * 1024.0)
            CobblemonSkinMod.LOGGER.info(
                "ResourcePackTransfer: prepared ${String.format("%.1f", sizeMB)}MB ZIP ($packHash)"
            )
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("ResourcePackTransfer: failed to prepare ZIP: ${e.message}")
            packZipBytes = ByteArray(0)
            packHash = ""
        }
    }

    fun getTotalChunks(): Int {
        if (packZipBytes.isEmpty()) return 0
        return (packZipBytes.size + CHUNK_SIZE - 1) / CHUNK_SIZE
    }

    fun getChunkData(chunkIndex: Int): ByteArray {
        val offset = chunkIndex * CHUNK_SIZE
        val length = minOf(CHUNK_SIZE, packZipBytes.size - offset)
        return packZipBytes.copyOfRange(offset, offset + length)
    }

    fun isReady(): Boolean = packZipBytes.isNotEmpty()
}
