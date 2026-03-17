package com.example.cobblemon_skin.config

import com.example.cobblemon_skin.CobblemonSkinMod
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import net.fabricmc.loader.api.FabricLoader
import java.io.File

/**
 * Reads/writes config/cobblemon_skin/skins.json.
 * Server admins can register new skins by adding entries here
 * without recompiling the mod.
 *
 * Example skins.json:
 * {
 *   "skins": [
 *     { "id": "pikachu_black",  "species": "cobblemon:pikachu",  "description": "Black Pikachu" },
 *     { "id": "charmander_ice", "species": "cobblemon:charmander","description": "Ice Charmander" }
 *   ]
 * }
 *
 * Each entry also requires:
 *   assets/cobblemon/bedrock/pokemon/variations/<id>.json  (variation descriptor)
 *   assets/cobblemon_skin/textures/pokemon/skins/<id>.png  (the texture)
 * Both should be inside a resource pack / data pack loaded by the server.
 */
object SkinConfig {

    private val GSON = GsonBuilder().setPrettyPrinting().create()

    private val configDir: File
        get() = FabricLoader.getInstance().configDir
            .resolve("cobblemon_skin")
            .toFile()

    private val configFile: File
        get() = configDir.resolve("skins.json")

    data class SkinEntry(
        @SerializedName("id") val id: String = "",
        @SerializedName("species") val species: String = "",
        @SerializedName("description") val description: String = ""
    )

    data class ConfigData(
        @SerializedName("skins") val skins: MutableList<SkinEntry> = mutableListOf()
    )

    fun load() {
        if (!configFile.exists()) {
            writeDefault()
        }

        try {
            val data = GSON.fromJson(configFile.readText(), ConfigData::class.java) ?: ConfigData()
            var count = 0
            for (entry in data.skins) {
                if (entry.id.isNotBlank()) {
                    CobblemonSkinMod.registerSkin(entry.id)
                    count++
                }
            }
            CobblemonSkinMod.LOGGER.info("CobblemonSkin: loaded $count skin(s) from config.")
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("CobblemonSkin: failed to read skins.json — ${e.message}")
        }
    }

    private fun writeDefault() {
        configDir.mkdirs()
        val default = ConfigData(
            skins = mutableListOf(
                SkinEntry("pikachu_black", "cobblemon:pikachu", "Black color variant for Pikachu"),
                SkinEntry("pikachu_red",   "cobblemon:pikachu", "Red color variant for Pikachu")
            )
        )
        configFile.writeText(GSON.toJson(default))
        CobblemonSkinMod.LOGGER.info("CobblemonSkin: created default skins.json at ${configFile.absolutePath}")
    }
}
