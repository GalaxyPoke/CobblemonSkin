package com.example.cobblemon_skin.server

import com.example.cobblemon_skin.CobblemonSkinMod
import com.example.cobblemon_skin.network.SkinInfo
import com.example.cobblemon_skin.network.SkinResourceData
import java.io.File

/**
 * Server-side skin manager. Scans skin packs from config/cobblemon_skin/skins/,
 * builds metadata list, and serves resource data on demand.
 */
object SkinManager {

    private val skinInfoList = mutableListOf<SkinInfo>()
    private val skinPacks = mutableMapOf<String, SkinPackData>()

    /** Internal representation of a loaded skin pack with file references. */
    private data class SkinPackData(
        val skinId: String,
        val species: String,
        val displayName: String,
        val quality: String,
        val description: String,
        val resolverFile: File?,
        val modelFile: File?,
        val textureFiles: Map<String, File>,  // relative path → file
        val poserFile: File?,
        val animationFile: File?,
        val packDir: File
    )

    val skinsDir: File by lazy {
        val gameDir = net.fabricmc.loader.api.FabricLoader.getInstance().gameDir.toFile()
        File(gameDir, "config/cobblemon_skin/skins")
    }

    fun getSkinInfoList(): List<SkinInfo> = skinInfoList.toList()

    fun getSkinResourceData(skinId: String): SkinResourceData? {
        if (!skinPacks.containsKey(skinId)) return null
        val files = ResourcePackTransfer.getSkinFiles(skinId)
        if (files.isEmpty()) return null
        return SkinResourceData(skinId = skinId, files = files)
    }

    /**
     * Scans all skin packs from the skins directory.
     * Called during server initialization.
     */
    fun loadAll() {
        skinInfoList.clear()
        skinPacks.clear()

        if (!skinsDir.exists()) {
            skinsDir.mkdirs()
            CobblemonSkinMod.LOGGER.info("Created skins directory: ${skinsDir.absolutePath}")
            return
        }

        val packDirs = skinsDir.listFiles()?.filter { it.isDirectory } ?: emptyList()

        for (packDir in packDirs) {
            try {
                val mainYml = File(packDir, "main.yml")
                if (!mainYml.exists()) continue

                val skinId = packDir.name
                val yml = parseMainYml(mainYml.readText())

                // Find resolver file
                val resolverFile = findFile(packDir, yml.resolvers)

                // Find model file
                val modelFile = findFile(packDir, yml.models)

                // Find texture files
                val textureFiles = mutableMapOf<String, File>()
                // Scan cobblemon/textures/ for all PNGs
                val texDir = File(packDir, "cobblemon/textures")
                if (texDir.exists()) {
                    texDir.walkTopDown().filter { it.isFile && it.extension == "png" }.forEach { f ->
                        val rel = f.relativeTo(File(packDir, "cobblemon")).path.replace('\\', '/')
                        textureFiles["cobblemon/$rel"] = f
                    }
                }
                // Also check flat textures
                packDir.listFiles()?.filter { it.extension == "png" }?.forEach { f ->
                    textureFiles["cobblemon/textures/pokemon/$skinId/${f.name}"] = f
                }

                // Find poser file
                val poserFile = findPoserFile(packDir, yml.posers)

                // Find animation file
                val animFile = findAnimationFile(packDir)

                // Parse metadata
                var desc = ""; var quality = "普通"; var obtain = ""; var detail = ""
                var inInfo = false
                for (line in mainYml.readText().lines()) {
                    val trimmed = line.trim()
                    if (trimmed == "info:") { inInfo = true; continue }
                    if (trimmed.endsWith(":") && !trimmed.startsWith("-")) { if (trimmed != "info:") inInfo = false; continue }
                    if (inInfo && trimmed.contains(":")) {
                        val k = trimmed.substringBefore(":").trim()
                        val v = trimmed.substringAfter(":").trim().removeSurrounding("\"").removeSurrounding("'")
                        when (k) {
                            "description" -> desc = v
                            "quality" -> quality = v
                            "obtain" -> obtain = v
                            "detail" -> detail = v
                        }
                    }
                }

                val species = yml.species.firstOrNull() ?: ""
                val displayName = skinId.replace("_", " ")

                val pack = SkinPackData(
                    skinId = skinId,
                    species = species,
                    displayName = displayName,
                    quality = quality,
                    description = detail.ifEmpty { desc },
                    resolverFile = resolverFile,
                    modelFile = modelFile,
                    textureFiles = textureFiles,
                    poserFile = poserFile,
                    animationFile = animFile,
                    packDir = packDir
                )

                skinPacks[skinId] = pack
                skinInfoList.add(SkinInfo(skinId, species, displayName, quality, detail.ifEmpty { desc }))

                // Register in CobblemonSkinMod for aspect/species mapping
                CobblemonSkinMod.registerSkin(skinId)
                if (species.isNotEmpty()) {
                    CobblemonSkinMod.skinSpeciesMap[skinId] = species
                }

            } catch (e: Exception) {
                CobblemonSkinMod.LOGGER.error("Failed to load skin pack '${packDir.name}': ${e.message}")
            }
        }

        CobblemonSkinMod.LOGGER.info("SkinManager: loaded ${skinInfoList.size} skin pack(s)")
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private data class ParsedYml(
        val models: List<String> = emptyList(),
        val animations: List<String> = emptyList(),
        val textures: List<String> = emptyList(),
        val resolvers: List<String> = emptyList(),
        val posers: List<String> = emptyList(),
        val species: List<String> = emptyList()
    )

    private fun parseMainYml(text: String): ParsedYml {
        val models = mutableListOf<String>()
        val animations = mutableListOf<String>()
        val textures = mutableListOf<String>()
        val resolvers = mutableListOf<String>()
        val posers = mutableListOf<String>()
        val species = mutableListOf<String>()
        var currentList: MutableList<String>? = null

        for (line in text.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
            when {
                trimmed.startsWith("models:") -> currentList = models
                trimmed.startsWith("animations:") -> currentList = animations
                trimmed.startsWith("textures:") -> currentList = textures
                trimmed.startsWith("resolvers:") -> currentList = resolvers
                trimmed.startsWith("posers:") -> currentList = posers
                trimmed.startsWith("species:") -> currentList = species
                trimmed.startsWith("- ") -> {
                    val value = trimmed.removePrefix("- ").trim().removeSurrounding("\"").removeSurrounding("'")
                    currentList?.add(value)
                }
                trimmed.endsWith(":") -> currentList = null
            }
        }
        return ParsedYml(models, animations, textures, resolvers, posers, species)
    }

    private fun findFile(packDir: File, paths: List<String>): File? {
        for (p in paths) {
            val f = File(packDir, p)
            if (f.exists()) return f
        }
        // Fallback: search recursively
        return packDir.walkTopDown().firstOrNull {
            it.isFile && (it.extension == "json" && paths.any { p -> it.name == File(p).name })
        }
    }

    private fun findPoserFile(packDir: File, paths: List<String>): File? {
        for (p in paths) {
            val f = File(packDir, p)
            if (f.exists()) return f
        }
        return packDir.walkTopDown().firstOrNull {
            it.isFile && it.extension == "json" && !it.name.endsWith(".geo.json") &&
                !it.name.endsWith(".animation.json") && it.parent?.contains("poser") == true
        }
    }

    private fun findAnimationFile(packDir: File): File? {
        return packDir.walkTopDown().firstOrNull {
            it.isFile && it.name.endsWith(".animation.json")
        }
    }
}
