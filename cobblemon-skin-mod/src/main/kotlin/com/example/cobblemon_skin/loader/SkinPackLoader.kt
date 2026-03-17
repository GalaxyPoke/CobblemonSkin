package com.example.cobblemon_skin.loader

import com.example.cobblemon_skin.CobblemonSkinMod
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import java.io.File

/**
 * Scans config/cobblemon_skin/skins/ for CobblemonSkinCore-format skin packs,
 * converts them into a standard Minecraft resource pack at
 * resourcepacks/cobblemon_skin_skins/, and registers skin IDs.
 *
 * Each skin pack folder should contain:
 *   main.yml          – file listing (models, textures, resolvers, etc.)
 *   cobblemon/...     – asset files mapped to the cobblemon namespace
 *
 * The loader rewrites resolver JSON files:
 *   - Replaces "cobblemonskincore:cobblemon/" with "cobblemon:"
 *   - Removes .json suffix from .geo.json model references
 *   - Replaces the custom aspect with "skin_<folderName>"
 */
object SkinPackLoader {

    private val GSON = GsonBuilder().setPrettyPrinting().create()
    private val STANDARD_ASPECTS = setOf(
        "shiny", "male", "female", "gmax",
        "mega", "mega-x", "mega-y", "primal",
        "alolan", "galarian", "hisuian", "paldean"
    )

    val skinsDir: File by lazy {
        FabricLoader.getInstance().configDir
            .resolve("cobblemon_skin/skins").toFile()
    }

    private val generatedPackDir: File by lazy {
        FabricLoader.getInstance().gameDir
            .resolve("resourcepacks/cobblemon_skin_skins").toFile()
    }

    private val manifestFile: File by lazy {
        File(generatedPackDir, ".manifest.json")
    }

    /**
     * Called during mod init. Uses manifest-based caching so only new/changed
     * skin packs are processed. Subsequent startups are near-instant.
     */
    fun loadAll() {
        if (!skinsDir.exists()) {
            skinsDir.mkdirs()
            CobblemonSkinMod.LOGGER.info("Created skins directory: ${skinsDir.absolutePath}")
            return
        }

        generatedPackDir.mkdirs()

        // Write pack.mcmeta if missing
        val mcmeta = File(generatedPackDir, "pack.mcmeta")
        if (!mcmeta.exists()) {
            mcmeta.writeText("""{"pack":{"pack_format":34,"description":"CobblemonSkin auto-generated skin pack"}}""")
        }

        // Merge extras FIRST so models/textures are available during resolver validation
        mergeExtrasDir()

        // Load existing manifest (skinId → lastModified timestamp)
        val oldManifest = loadManifest()
        val newManifest = mutableMapOf<String, Long>()

        val packDirs = skinsDir.listFiles()?.filter { it.isDirectory } ?: emptyList()
        var processed = 0
        var cached = 0
        var removed = 0

        // Detect removed packs
        val currentIds = packDirs.map { it.name }.toSet()
        for (oldId in oldManifest.keys) {
            if (oldId !in currentIds) {
                // Pack was removed – clean up its resolver files (best-effort)
                removed++
                CobblemonSkinMod.LOGGER.info("Skin pack '$oldId' removed")
            }
        }

        for (packDir in packDirs) {
            val skinId = packDir.name
            val lastMod = packDir.lastModified()
            try {
                val oldMod = oldManifest[skinId]
                if (oldMod != null && oldMod == lastMod) {
                    // Already processed, just re-register in memory
                    registerFromMainYml(packDir, skinId)
                    cached++
                } else {
                    // New or changed – full processing
                    if (processPack(packDir)) processed++
                }
                newManifest[skinId] = lastMod
            } catch (e: Exception) {
                CobblemonSkinMod.LOGGER.error("Failed to process skin pack '$skinId': ${e.message}")
                // Still record in manifest to avoid retrying broken packs every time
                newManifest[skinId] = lastMod
            }
        }

        // Post-process: fix ALL resolver files in the generated pack
        // (some may have been overwritten by later packs' copyRecursively)
        if (processed > 0) {
            postProcessAllResolvers()
        }

        // Save updated manifest
        saveManifest(newManifest)

        val total = processed + cached
        if (total == 0 && generatedPackDir.exists()) {
            generatedPackDir.deleteRecursively()
        } else if (total > 0) {
            ensurePackEnabled()
        }

        CobblemonSkinMod.LOGGER.info("SkinPackLoader: $total skin(s) loaded ($processed new/changed, $cached cached, $removed removed)")
    }

    /**
     * Post-process pass: scan ALL resolver JSON files in the generated pack
     * and fix any remaining cobblemonskincore: references + validate models.
     * This handles files overwritten by later packs' copyRecursively.
     */
    private fun postProcessAllResolvers() {
        val resolversDir = File(generatedPackDir, "assets/cobblemon/bedrock/pokemon/resolvers")
        if (!resolversDir.exists()) return

        var fixed = 0
        resolversDir.walkTopDown().filter { it.isFile && it.extension == "json" }.forEach { file ->
            try {
                var text = file.readText()
                if (!text.contains("cobblemonskincore")) return@forEach

                // Phase 1: text-level namespace replacement (handles all texture refs including animated objects)
                text = text.replace("cobblemonskincore:cobblemon/", "cobblemon:")
                text = text.replace("cobblemonskincore:", "cobblemon:")

                // Phase 2: parse JSON to clean up model paths (full path → filename.geo) + validate
                val root = JsonParser.parseString(text).asJsonObject
                val variations = root.getAsJsonArray("variations") ?: run {
                    file.writeText(text)
                    fixed++
                    return@forEach
                }

                for (element in variations) {
                    if (!element.isJsonObject) continue
                    val variation = element.asJsonObject

                    // Fix model reference: cobblemon:bedrock/pokemon/models/xxx.geo.json → cobblemon:xxx.geo
                    if (variation.has("model")) {
                        val modelVal = variation.get("model")
                        if (modelVal.isJsonPrimitive) {
                            var model = modelVal.asString
                            if (model.contains("/")) {
                                val fileName = File(model.substringAfter(":")).name
                                val cleanName = if (fileName.endsWith(".geo.json")) fileName.removeSuffix(".json") else fileName
                                model = "cobblemon:$cleanName"
                            }
                            // Validate model exists in our pack
                            val shortName = model.substringAfter(":")
                            val modelFileName = if (shortName.endsWith(".geo")) "$shortName.json" else "$shortName.geo.json"
                            val modelFile = File(generatedPackDir, "assets/cobblemon/bedrock/pokemon/models/$modelFileName")
                            if (modelFile.exists()) {
                                variation.addProperty("model", model)
                            } else {
                                variation.remove("model")
                            }
                        }
                    }

                    // Fix poser reference: remove full path, strip .json
                    if (variation.has("poser")) {
                        val poserVal = variation.get("poser")
                        if (poserVal.isJsonPrimitive) {
                            var poser = poserVal.asString
                            if (poser.endsWith(".json")) poser = poser.removeSuffix(".json")
                            variation.addProperty("poser", poser)
                        }
                    }
                }

                file.writeText(GSON.toJson(root))
                fixed++
            } catch (e: Exception) {
                // If JSON parsing fails, still write the text-replaced version
                try {
                    var text = file.readText()
                    text = text.replace("cobblemonskincore:cobblemon/", "cobblemon:")
                    text = text.replace("cobblemonskincore:", "cobblemon:")
                    file.writeText(text)
                    fixed++
                } catch (_: Exception) {}
                CobblemonSkinMod.LOGGER.warn("Post-process partial fix for ${file.name}: ${e.message}")
            }
        }

        if (fixed > 0) {
            CobblemonSkinMod.LOGGER.info("Post-processed $fixed resolver file(s) with cobblemonskincore references")
        }
    }

    /**
     * Merges assets from config/cobblemon_skin/extras/ into the generated pack.
     * Place shared assets (e.g., cobble-caf-forms) under extras/assets/cobblemon/
     * They will be copied into the generated resource pack without overwriting existing files.
     */
    private fun mergeExtrasDir() {
        val extrasDir = File(skinsDir.parentFile, "extras")
        if (!extrasDir.exists()) return
        var count = 0
        var errors = 0
        try {
            extrasDir.walkTopDown().filter { it.isFile }.forEach { src ->
                try {
                    val rel = src.relativeTo(extrasDir)
                    val dest = File(generatedPackDir, rel.path)
                    if (!dest.exists()) {
                        dest.parentFile.mkdirs()
                        src.copyTo(dest)
                        count++
                    }
                } catch (e: Exception) {
                    errors++
                    if (errors <= 3) CobblemonSkinMod.LOGGER.warn("Extras merge failed for ${src.name}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("Extras merge walk failed: ${e.message}")
        }
        CobblemonSkinMod.LOGGER.info("Merged $count file(s) from extras directory ($errors errors)")
    }

    /**
     * Re-registers a cached skin pack from main.yml without copying files.
     */
    private fun registerFromMainYml(packDir: File, skinId: String) {
        val mainYml = File(packDir, "main.yml")
        if (!mainYml.exists()) return
        val yml = parseMainYml(mainYml.readText())

        CobblemonSkinMod.registerSkin(skinId)
        var speciesId = yml.species.firstOrNull()
        if (speciesId == null) {
            // Try resolvers in generated pack
            val isFlat = !File(packDir, "cobblemon").exists()
            val resolverPaths = if (isFlat) {
                yml.resolvers.map { "cobblemon/bedrock/pokemon/resolvers/$skinId/${File(it).name}" }
            } else {
                yml.resolvers
            }
            speciesId = extractSpeciesFromResolvers(resolverPaths)
        }
        if (speciesId != null) CobblemonSkinMod.skinSpeciesMap[skinId] = speciesId
        if (yml.uiScale != 1.0f || yml.uiOffsetX != 0 || yml.uiOffsetY != 0) {
            CobblemonSkinMod.skinUiConfigs[skinId] = CobblemonSkinMod.SkinUiConfig(yml.uiScale, yml.uiOffsetX, yml.uiOffsetY)
        }
        CobblemonSkinMod.skinMetaMap[skinId] = CobblemonSkinMod.SkinMeta(yml.description, yml.quality, yml.obtain, yml.detail)
    }

    private fun loadManifest(): Map<String, Long> {
        if (!manifestFile.exists()) return emptyMap()
        return try {
            val root = JsonParser.parseString(manifestFile.readText()).asJsonObject
            root.entrySet().associate { it.key to it.value.asLong }
        } catch (_: Exception) { emptyMap() }
    }

    private fun saveManifest(manifest: Map<String, Long>) {
        val obj = com.google.gson.JsonObject()
        for ((k, v) in manifest) obj.addProperty(k, v)
        manifestFile.writeText(GSON.toJson(obj))
    }

    /**
     * Ensures the generated resource pack is in options.txt so Minecraft loads it
     * at startup, avoiding a costly mid-game resource reload that can cause OOM.
     */
    private fun ensurePackEnabled() {
        val packEntry = "file/cobblemon_skin_skins"
        try {
            val optionsFile = FabricLoader.getInstance().gameDir.resolve("options.txt").toFile()
            if (!optionsFile.exists()) return

            val lines = optionsFile.readLines().toMutableList()
            var found = false
            for (i in lines.indices) {
                if (lines[i].startsWith("resourcePacks:")) {
                    found = true
                    val value = lines[i].removePrefix("resourcePacks:")
                    if (value.contains(packEntry)) {
                        CobblemonSkinMod.LOGGER.info("Resource pack '$packEntry' already enabled in options.txt")
                        return
                    }
                    // Insert before closing bracket
                    val newValue = if (value.trim() == "[]") {
                        "[\"$packEntry\"]"
                    } else {
                        value.trimEnd().removeSuffix("]") + ",\"$packEntry\"]"
                    }
                    lines[i] = "resourcePacks:$newValue"
                    break
                }
            }

            if (!found) {
                lines.add("resourcePacks:[\"$packEntry\"]")
            }

            optionsFile.writeText(lines.joinToString("\n"))
            CobblemonSkinMod.LOGGER.info("Auto-enabled resource pack '$packEntry' in options.txt")
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.warn("Failed to auto-enable resource pack in options.txt: ${e.message}")
        }
    }

    private fun processPack(packDir: File): Boolean {
        val mainYml = File(packDir, "main.yml")
        if (!mainYml.exists()) {
            CobblemonSkinMod.LOGGER.warn("Skin pack '${packDir.name}' has no main.yml, skipping")
            return false
        }

        val skinId = packDir.name
        val yml = parseMainYml(mainYml.readText())

        // Copy ONLY files listed in main.yml + unlisted textures.
        // Do NOT blindly copyRecursively - that copies base models/resolvers that override Cobblemon defaults.
        val isFlat = !File(packDir, "cobblemon").exists()
        if (isFlat) {
            for (p in yml.models)     copyFlat(packDir, p, "cobblemon/bedrock/pokemon/models")
            for (p in yml.animations) copyFlat(packDir, p, "cobblemon/bedrock/pokemon/animations")
            for (p in yml.textures)   copyFlat(packDir, p, "cobblemon/textures/pokemon/$skinId")
            for (p in yml.resolvers)  copyFlat(packDir, p, "cobblemon/bedrock/pokemon/resolvers/$skinId")
            for (p in yml.posers)     copyFlat(packDir, p, "cobblemon/bedrock/pokemon/posers")
            // Also copy unlisted texture/poser files from pack root
            packDir.listFiles()?.forEach { f ->
                when {
                    f.extension == "png" && f.name !in yml.textures.map { File(it).name } -> {
                        val dest = File(generatedPackDir, "assets/cobblemon/textures/pokemon/$skinId/${f.name}")
                        dest.parentFile.mkdirs()
                        f.copyTo(dest, overwrite = true)
                    }
                    f.name.endsWith(".json") && !f.name.endsWith(".geo.json")
                        && !f.name.endsWith(".animation.json")
                        && f.name !in yml.resolvers.map { File(it).name }
                        && f.name != "main.yml" -> {
                        val dest = File(generatedPackDir, "assets/cobblemon/bedrock/pokemon/posers/${skinId}_${f.name}")
                        dest.parentFile.mkdirs()
                        f.copyTo(dest, overwrite = true)
                    }
                }
            }
        } else {
            // Structured format: copy ONLY listed files from main.yml
            for (p in yml.models + yml.animations + yml.textures + yml.resolvers + yml.posers) {
                val src = File(packDir, p)
                if (!src.exists()) continue
                val dest = File(generatedPackDir, "assets/$p")
                dest.parentFile.mkdirs()
                src.copyTo(dest, overwrite = true)
            }
            // Also copy ALL texture .png files from cobblemon/textures/ (safe, non-behavioral)
            val texSrc = File(packDir, "cobblemon/textures")
            if (texSrc.exists()) {
                texSrc.walkTopDown().filter { it.isFile && it.extension == "png" }.forEach { f ->
                    val rel = f.relativeTo(File(packDir, "cobblemon"))
                    val dest = File(generatedPackDir, "assets/cobblemon/$rel")
                    dest.parentFile.mkdirs()
                    f.copyTo(dest, overwrite = true)
                }
            }
        }

        // Locate and rewrite all resolver files in generated pack
        val resolverPaths = if (isFlat) {
            yml.resolvers.map { "cobblemon/bedrock/pokemon/resolvers/$skinId/${File(it).name}" }
        } else {
            yml.resolvers
        }
        for (resolverPath in resolverPaths) {
            val resolverFile = File(generatedPackDir, "assets/$resolverPath")
            if (resolverFile.exists()) {
                rewriteResolver(resolverFile, skinId)
            }
        }

        // Register the skin and its species mapping
        CobblemonSkinMod.registerSkin(skinId)
        var speciesId = yml.species.firstOrNull()

        // If main.yml has no species, try to extract from resolver JSON files
        if (speciesId == null) {
            speciesId = extractSpeciesFromResolvers(resolverPaths)
        }

        if (speciesId != null) {
            CobblemonSkinMod.skinSpeciesMap[skinId] = speciesId
        }

        // Store per-skin UI config
        if (yml.uiScale != 1.0f || yml.uiOffsetX != 0 || yml.uiOffsetY != 0) {
            CobblemonSkinMod.skinUiConfigs[skinId] = CobblemonSkinMod.SkinUiConfig(yml.uiScale, yml.uiOffsetX, yml.uiOffsetY)
        }

        // Store skin metadata
        CobblemonSkinMod.skinMetaMap[skinId] = CobblemonSkinMod.SkinMeta(yml.description, yml.quality, yml.obtain, yml.detail)

        CobblemonSkinMod.LOGGER.info("Loaded skin pack '$skinId' (species: ${speciesId ?: "unknown"}, ui: scale=${yml.uiScale} offset=${yml.uiOffsetX},${yml.uiOffsetY})")
        return true
    }

    /**
     * Reads resolver JSON files and extracts the "species" field (e.g. "cobblemon:gyarados").
     * This is used as a fallback when main.yml doesn't have a species entry.
     */
    private fun extractSpeciesFromResolvers(resolverPaths: List<String>): String? {
        for (resolverPath in resolverPaths) {
            try {
                val resolverFile = File(generatedPackDir, "assets/$resolverPath")
                if (!resolverFile.exists()) continue
                val root = JsonParser.parseString(resolverFile.readText()).asJsonObject
                if (root.has("species")) {
                    val species = root.get("species").asString
                    if (species.isNotBlank()) return species
                }
                // Also check "name" field (alternative format)
                if (root.has("name")) {
                    val name = root.get("name").asString
                    if (name.isNotBlank()) return name
                }
            } catch (_: Exception) {}
        }
        return null
    }

    /**
     * Copy a flat file (just a filename, no directory structure) into the correct
     * resource pack location under the given destSubDir.
     */
    private fun copyFlat(packDir: File, relativePath: String, destSubDir: String) {
        val fileName = File(relativePath).name
        val src = File(packDir, fileName)
        if (!src.exists()) return
        val dest = File(generatedPackDir, "assets/$destSubDir/$fileName")
        dest.parentFile.mkdirs()
        src.copyTo(dest, overwrite = true)
    }

    /**
     * Collects all model filenames present in the generated resource pack.
     * Cached after first call.
     */
    private var _modelCache: Set<String>? = null
    private fun availableModels(): Set<String> {
        if (_modelCache != null) return _modelCache!!
        val modelsDir = File(generatedPackDir, "assets/cobblemon/bedrock/pokemon/models")
        _modelCache = if (modelsDir.exists()) {
            modelsDir.listFiles()
                ?.filter { it.extension == "json" }
                ?.map { it.nameWithoutExtension.let { n -> if (n.endsWith(".geo")) n else n } }
                ?.toSet() ?: emptySet()
        } else emptySet()
        return _modelCache!!
    }

    private var _poserCache: Set<String>? = null
    private fun availablePosers(): Set<String> {
        if (_poserCache != null) return _poserCache!!
        val posersDir = File(generatedPackDir, "assets/cobblemon/bedrock/pokemon/posers")
        _poserCache = if (posersDir.exists()) {
            posersDir.listFiles()
                ?.filter { it.extension == "json" }
                ?.map { it.nameWithoutExtension }
                ?.toSet() ?: emptySet()
        } else emptySet()
        return _poserCache!!
    }

    private fun rewriteResolver(file: File, skinId: String) {
        try {
            // Phase 1: text-level namespace replacement (handles animated textures, layers, etc.)
            var text = file.readText()
            text = text.replace("cobblemonskincore:cobblemon/", "cobblemon:")
            text = text.replace("cobblemonskincore:", "cobblemon:")

            // Phase 2: parse JSON for aspect rewriting + model path cleanup + validation
            val root = JsonParser.parseString(text).asJsonObject
            val variations = root.getAsJsonArray("variations") ?: run {
                file.writeText(text)
                return
            }

            for (element in variations) {
                if (!element.isJsonObject) continue
                val variation = element.asJsonObject

                // Rewrite aspects: replace custom aspects with skin_<skinId>, deduplicate
                val aspects = variation.getAsJsonArray("aspects")
                if (aspects != null) {
                    val skinAspect = CobblemonSkinMod.aspectFor(skinId)
                    var hasCustom = false
                    for (i in 0 until aspects.size()) {
                        val aspect = aspects[i].asString
                        if (aspect !in STANDARD_ASPECTS) {
                            aspects.set(i, com.google.gson.JsonPrimitive(skinAspect))
                            hasCustom = true
                        }
                    }
                    if (!hasCustom) {
                        aspects.add(com.google.gson.JsonPrimitive(skinAspect))
                    }
                    // Deduplicate aspects
                    val unique = mutableListOf<String>()
                    for (i in 0 until aspects.size()) {
                        val a = aspects[i].asString
                        if (a !in unique) unique.add(a)
                    }
                    while (aspects.size() > 0) aspects.remove(0)
                    unique.forEach { aspects.add(com.google.gson.JsonPrimitive(it)) }
                }

                // Fix model reference: full path → filename.geo + validate
                if (variation.has("model")) {
                    val modelVal = variation.get("model")
                    if (modelVal.isJsonPrimitive) {
                        var model = modelVal.asString
                        if (model.contains("/")) {
                            val fileName = File(model.substringAfter(":")).name
                            val cleanName = if (fileName.endsWith(".geo.json")) fileName.removeSuffix(".json") else fileName
                            model = "cobblemon:$cleanName"
                        }
                        val shortName = model.substringAfter(":")
                        val modelFileName = if (shortName.endsWith(".geo")) "$shortName.json" else "$shortName.geo.json"
                        val modelFile = File(generatedPackDir, "assets/cobblemon/bedrock/pokemon/models/$modelFileName")
                        if (modelFile.exists()) {
                            variation.addProperty("model", model)
                        } else {
                            variation.remove("model")
                        }
                    }
                }

                // Fix short texture paths: cobblemon:xxx.png → cobblemon:textures/pokemon/<skinId>/xxx.png
                fixShortTexturePath(variation, "texture", skinId)
                val layers = variation.getAsJsonArray("layers")
                if (layers != null) {
                    for (layerElem in layers) {
                        if (layerElem.isJsonObject) {
                            fixShortTexturePath(layerElem.asJsonObject, "texture", skinId)
                        }
                    }
                }

                // Fix poser reference: strip .json
                if (variation.has("poser")) {
                    val poserVal = variation.get("poser")
                    if (poserVal.isJsonPrimitive) {
                        var poser = poserVal.asString
                        if (poser.endsWith(".json")) poser = poser.removeSuffix(".json")
                        val poserName = poser.removePrefix("cobblemon:")
                        if (poserName.contains("/")) {
                            val poserFile = File(generatedPackDir, "assets/cobblemon/$poserName.json")
                            if (!poserFile.exists()) {
                                variation.remove("poser")
                            } else {
                                variation.addProperty("poser", poser)
                            }
                        } else {
                            variation.addProperty("poser", poser)
                        }
                    }
                }
            }

            file.writeText(GSON.toJson(root))
        } catch (e: Exception) {
            // Fallback: at minimum do the text-level replacement
            try {
                var text = file.readText()
                text = text.replace("cobblemonskincore:cobblemon/", "cobblemon:")
                text = text.replace("cobblemonskincore:", "cobblemon:")
                file.writeText(text)
            } catch (_: Exception) {}
            CobblemonSkinMod.LOGGER.error("Failed to rewrite resolver ${file.name}: ${e.message}")
        }
    }

    private fun fixShortTexturePath(obj: com.google.gson.JsonObject, key: String, skinId: String) {
        if (!obj.has(key)) return
        val texVal = obj.get(key)
        if (!texVal.isJsonPrimitive) return // skip animated texture objects
        val tex = texVal.asString
        val path = tex.substringAfter(":")
        // Short path: no "textures/" prefix → fix to textures/pokemon/<skinId>/<filename>
        if (tex.startsWith("cobblemon:") && !path.startsWith("textures/") && path.endsWith(".png")) {
            obj.addProperty(key, "cobblemon:textures/pokemon/$skinId/$path")
        }
    }

    // ── Simple main.yml parser ─────────────────────────────────────────────────
    data class SkinPackYml(
        val models: List<String> = emptyList(),
        val animations: List<String> = emptyList(),
        val textures: List<String> = emptyList(),
        val resolvers: List<String> = emptyList(),
        val posers: List<String> = emptyList(),
        val species: List<String> = emptyList(),
        val uiScale: Float = 1.0f,
        val uiOffsetX: Int = 0,
        val uiOffsetY: Int = 0,
        val description: String = "",
        val quality: String = "普通",
        val obtain: String = "",
        val detail: String = ""
    )

    private fun parseMainYml(text: String): SkinPackYml {
        val models = mutableListOf<String>()
        val animations = mutableListOf<String>()
        val textures = mutableListOf<String>()
        val resolvers = mutableListOf<String>()
        val posers = mutableListOf<String>()
        val species = mutableListOf<String>()
        var uiScale = 1.0f
        var uiOffsetX = 0
        var uiOffsetY = 0
        var description = ""
        var quality = "普通"
        var obtain = ""
        var detail = ""

        var currentKey = ""
        var inUiBlock = false
        var inInfoBlock = false
        for (rawLine in text.lines()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) continue

            // Top-level or nested key (e.g. "models:", "  models:")
            if (line.endsWith(":") && !line.startsWith("-")) {
                currentKey = line.removeSuffix(":").trim()
                inUiBlock = currentKey == "ui"
                inInfoBlock = currentKey == "info"
                continue
            }

            // UI block: key-value pairs like "scale: 1.5"
            if (inUiBlock && line.contains(":") && !line.contains("[")) {
                val key = line.substringBefore(":").trim()
                val value = line.substringAfter(":").trim()
                when (key) {
                    "scale" -> uiScale = value.toFloatOrNull() ?: 1.0f
                    "offsetX" -> uiOffsetX = value.toIntOrNull() ?: 0
                    "offsetY" -> uiOffsetY = value.toIntOrNull() ?: 0
                }
                continue
            }

            // Info block: key-value pairs like "description: 新春限定"
            if (inInfoBlock && line.contains(":") && !line.contains("[")) {
                val key = line.substringBefore(":").trim()
                val value = line.substringAfter(":").trim().removeSurrounding("\"").removeSurrounding("'")
                when (key) {
                    "description" -> description = value
                    "quality" -> quality = value
                    "obtain" -> obtain = value
                    "detail" -> detail = value
                }
                continue
            }

            // Inline array: key: ["a", "b"]
            if (line.contains(":") && line.contains("[")) {
                val key = line.substringBefore(":").trim()
                val arrayPart = line.substringAfter("[").substringBefore("]")
                val items = arrayPart.split(",")
                    .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                    .filter { it.isNotBlank() }
                addToList(key, items, models, animations, textures, resolvers, posers, species)
                inUiBlock = false
                continue
            }

            // List item: - "value"
            if (line.startsWith("-")) {
                val value = line.removePrefix("-").trim()
                    .removeSurrounding("\"").removeSurrounding("'")
                if (value.isNotBlank()) {
                    addToList(currentKey, listOf(value), models, animations, textures, resolvers, posers, species)
                }
                inUiBlock = false
            }
        }

        return SkinPackYml(models, animations, textures, resolvers, posers, species, uiScale, uiOffsetX, uiOffsetY, description, quality, obtain, detail)
    }

    private fun addToList(
        key: String, values: List<String>,
        models: MutableList<String>, animations: MutableList<String>,
        textures: MutableList<String>, resolvers: MutableList<String>,
        posers: MutableList<String>, species: MutableList<String>
    ) {
        when (key) {
            "models" -> models.addAll(values)
            "animations" -> animations.addAll(values)
            "textures" -> textures.addAll(values)
            "resolvers" -> resolvers.addAll(values)
            "posers" -> posers.addAll(values)
            "species" -> species.addAll(values)
        }
    }
}
