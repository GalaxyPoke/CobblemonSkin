package com.example.cobblemon_skin.server

import com.example.cobblemon_skin.CobblemonSkinMod
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Two-phase resource pack transfer:
 *
 * Phase 1 (on join): Resolver-only pack — contains pack.mcmeta, skin_list.json,
 *   and all resolver JSON files (~100KB for 84 skins). Sent via chunk streaming.
 *   Client extracts → Cobblemon knows all skin variations exist.
 *
 * Phase 2 (on demand): Per-skin files — model, textures, posers, animations.
 *   Sent individually when client requests a specific skin.
 *   Client saves to cache → triggers resource reload → skin renders correctly.
 */
object ResourcePackTransfer {

    /** Hash of the resolver-only pack (used for client-side caching). */
    var resolverPackHash: String = ""
        private set

    /** Hash of the asset pack (models+textures+posers+animations). */
    var assetPackHash: String = ""
        private set

    /** Chunk size: 256KB per packet. */
    const val CHUNK_SIZE = 256 * 1024

    private var resolverPackFile: File? = null
    private var resolverPackFileSize: Long = 0

    private var assetPackFile: File? = null
    private var assetPackFileSize: Long = 0

    val generatedPackDir: File by lazy {
        FabricLoader.getInstance().gameDir.resolve("resourcepacks/cobblemon_skin_skins").toFile()
    }

    /**
     * Called after SkinPackLoader.loadAll(). Creates:
     *   1. Resolver-only ZIP for initial client sync (small, fast)
     *   2. Keeps generated pack dir on disk for per-skin file serving
     */
    fun prepare() {
        if (!generatedPackDir.exists() || generatedPackDir.listFiles().isNullOrEmpty()) {
            resolverPackHash = ""
            assetPackHash = ""
            CobblemonSkinMod.LOGGER.info("ResourcePackTransfer: no resource pack to serve")
            return
        }

        try {
            val gameDir = FabricLoader.getInstance().gameDir.toFile()

            // Resolver-only ZIP: pack.mcmeta + skin_list.json + all resolvers (~100KB)
            val resolverZip = File(gameDir, "cobblemon_skin_resolvers.zip")
            zipDirectory(generatedPackDir, resolverZip) { file ->
                val rel = file.relativeTo(generatedPackDir).path.replace('\\', '/')
                rel == "pack.mcmeta" ||
                    rel == "skin_list.json" ||
                    rel.startsWith("assets/cobblemon/bedrock/pokemon/resolvers/")
            }
            resolverPackFile = resolverZip
            resolverPackFileSize = resolverZip.length()
            resolverPackHash = computeHash(resolverZip)

            // Asset ZIP: models + textures + posers + animations (everything else)
            val assetZip = File(gameDir, "cobblemon_skin_assets.zip")
            zipDirectory(generatedPackDir, assetZip) { file ->
                val rel = file.relativeTo(generatedPackDir).path.replace('\\', '/')
                rel != "pack.mcmeta" &&
                    rel != "skin_list.json" &&
                    !rel.startsWith("assets/cobblemon/bedrock/pokemon/resolvers/") &&
                    !rel.startsWith(".")
            }
            assetPackFile = assetZip
            assetPackFileSize = assetZip.length()
            assetPackHash = computeHash(assetZip)

            val resolverKB = resolverPackFileSize / 1024.0
            val assetMB = assetPackFileSize / (1024.0 * 1024.0)
            CobblemonSkinMod.LOGGER.info(
                "ResourcePackTransfer: resolver=${String.format("%.1f", resolverKB)}KB, assets=${String.format("%.1f", assetMB)}MB"
            )
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("ResourcePackTransfer: failed to prepare: ${e.message}", e)
            resolverPackHash = ""
            assetPackHash = ""
        }
    }

    // ── Chunk streaming (packType: 0=resolver, 1=asset) ──────────────────────

    fun getTotalChunks(packType: Int): Int {
        val size = if (packType == 0) resolverPackFileSize else assetPackFileSize
        if (size <= 0) return 0
        return ((size + CHUNK_SIZE - 1) / CHUNK_SIZE).toInt()
    }

    fun readChunk(packType: Int, chunkIndex: Int): ByteArray {
        val file = if (packType == 0) resolverPackFile else assetPackFile
        val fileSize = if (packType == 0) resolverPackFileSize else assetPackFileSize
        if (file == null) return ByteArray(0)
        val offset = chunkIndex.toLong() * CHUNK_SIZE
        val length = minOf(CHUNK_SIZE.toLong(), fileSize - offset).toInt()
        if (length <= 0) return ByteArray(0)

        RandomAccessFile(file, "r").use { raf ->
            raf.seek(offset)
            val buf = ByteArray(length)
            raf.readFully(buf)
            return buf
        }
    }

    fun isReady(): Boolean = resolverPackFile != null && resolverPackHash.isNotEmpty()

    // ── Per-skin file serving (on demand) ────────────────────────────────────

    /**
     * Reads all non-resolver files for a specific skin from the generated pack.
     * Returns a map of relativePath → fileContent (models, textures, posers, animations).
     */
    fun getSkinFiles(skinId: String): Map<String, ByteArray> {
        val files = mutableMapOf<String, ByteArray>()
        val skinAspect = CobblemonSkinMod.aspectFor(skinId)

        // 1. Find and parse resolver to discover model/poser references
        val resolversDir = File(generatedPackDir, "assets/cobblemon/bedrock/pokemon/resolvers")
        if (resolversDir.exists()) {
            for (rFile in resolversDir.walkTopDown().filter { it.isFile && it.extension == "json" }) {
                try {
                    val root = JsonParser.parseString(rFile.readText()).asJsonObject
                    val variations = root.getAsJsonArray("variations") ?: continue
                    var matched = false
                    for (elem in variations) {
                        if (!elem.isJsonObject) continue
                        val v = elem.asJsonObject
                        val aspects = v.getAsJsonArray("aspects") ?: continue
                        val hasAspect = (0 until aspects.size()).any { aspects[it].asString == skinAspect }
                        if (!hasAspect) continue
                        matched = true

                        // Model
                        if (v.has("model") && v.get("model").isJsonPrimitive) {
                            val model = v.get("model").asString
                            val shortName = model.substringAfter(":")
                            for (ext in listOf("$shortName.json", "$shortName.geo.json")) {
                                val mf = File(generatedPackDir, "assets/cobblemon/bedrock/pokemon/models/$ext")
                                if (mf.exists()) {
                                    files[mf.relativeTo(generatedPackDir).path.replace('\\', '/')] = mf.readBytes()
                                    break
                                }
                            }
                        }

                        // Poser
                        if (v.has("poser") && v.get("poser").isJsonPrimitive) {
                            val poser = v.get("poser").asString.substringAfter(":")
                            val pf = File(generatedPackDir, "assets/cobblemon/bedrock/pokemon/posers/$poser.json")
                            if (pf.exists()) {
                                files[pf.relativeTo(generatedPackDir).path.replace('\\', '/')] = pf.readBytes()
                            }
                        }

                        // Collect texture references from variation
                        collectTextureRefs(v, files)
                    }
                    if (matched) break
                } catch (_: Exception) {}
            }
        }

        // 2. Scan texture directory for this skin (catches files not referenced in resolver)
        val texDir = File(generatedPackDir, "assets/cobblemon/textures/pokemon/$skinId")
        if (texDir.exists()) {
            texDir.walkTopDown().filter { it.isFile }.forEach { f ->
                val rel = f.relativeTo(generatedPackDir).path.replace('\\', '/')
                if (rel !in files) files[rel] = f.readBytes()
            }
        }

        // 3. Animations: scan for files matching skinId
        val animDir = File(generatedPackDir, "assets/cobblemon/bedrock/pokemon/animations")
        if (animDir.exists()) {
            animDir.listFiles()?.filter { it.isFile && it.name.contains(skinId, ignoreCase = true) }?.forEach { f ->
                val rel = f.relativeTo(generatedPackDir).path.replace('\\', '/')
                if (rel !in files) files[rel] = f.readBytes()
            }
        }

        return files
    }

    /**
     * Collects texture file references from a resolver variation object.
     */
    private fun collectTextureRefs(variation: com.google.gson.JsonObject, files: MutableMap<String, ByteArray>) {
        // Main texture
        collectSingleTexture(variation, "texture", files)
        // Layer textures
        val layers = variation.getAsJsonArray("layers")
        if (layers != null) {
            for (layerElem in layers) {
                if (layerElem.isJsonObject) {
                    collectSingleTexture(layerElem.asJsonObject, "texture", files)
                }
            }
        }
    }

    private fun collectSingleTexture(obj: com.google.gson.JsonObject, key: String, files: MutableMap<String, ByteArray>) {
        if (!obj.has(key)) return
        val texVal = obj.get(key)
        if (!texVal.isJsonPrimitive) return
        val tex = texVal.asString
        val path = tex.substringAfter(":")
        if (path.isNotEmpty()) {
            val tf = File(generatedPackDir, "assets/cobblemon/$path")
            if (tf.exists()) {
                val rel = tf.relativeTo(generatedPackDir).path.replace('\\', '/')
                files[rel] = tf.readBytes()
            }
        }
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    private fun zipDirectory(sourceDir: File, outputZip: File, filter: (File) -> Boolean) {
        FileOutputStream(outputZip).use { fos ->
            ZipOutputStream(fos).use { zos ->
                sourceDir.walkTopDown().filter { it.isFile && filter(it) }.forEach { file ->
                    val entryName = file.relativeTo(sourceDir).path.replace('\\', '/')
                    zos.putNextEntry(ZipEntry(entryName))
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }
    }

    private fun computeHash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buf = ByteArray(8192)
            var n: Int
            while (fis.read(buf).also { n = it } != -1) {
                digest.update(buf, 0, n)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
