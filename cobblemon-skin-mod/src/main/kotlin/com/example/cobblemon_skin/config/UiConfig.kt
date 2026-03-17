package com.example.cobblemon_skin.config

import com.example.cobblemon_skin.CobblemonSkinMod
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import net.fabricmc.loader.api.FabricLoader
import java.io.File

/**
 * 读写 config/cobblemon_skin/ui_config.json。
 * 控制皮肤 GUI 中模型渲染的默认缩放和偏移。
 *
 * 示例 ui_config.json：
 * {
 *   "partyCard":     { "scale": 1.5, "offsetX": 0, "offsetY": 0 },
 *   "skinPreview":   { "scale": 1.5, "offsetX": 0, "offsetY": 0 },
 *   "allSkinsGrid":  { "scale": 1.5, "offsetX": 0, "offsetY": 0 }
 * }
 *
 * partyCard     — 队伍页宝可梦卡片的小模型
 * skinPreview   — 点击宝可梦后右侧皮肤列表的预览模型
 * allSkinsGrid  — "全部皮肤"页网格中的预览模型
 *
 * scale   — 模型缩放倍率（越大模型越大）
 * offsetX — 水平偏移像素（正=右，负=左）
 * offsetY — 垂直偏移像素（正=下，负=上）
 */
object UiConfig {

    private val GSON = GsonBuilder().setPrettyPrinting().create()

    private val configFile: File
        get() = FabricLoader.getInstance().configDir
            .resolve("cobblemon_skin/ui_config.json")
            .toFile()

    data class ModelRenderConfig(
        @SerializedName("scale") val scale: Float = 1.0f,
        @SerializedName("offsetX") val offsetX: Int = 0,
        @SerializedName("offsetY") val offsetY: Int = 0
    )

    data class UiConfigData(
        @SerializedName("partyCard") val partyCard: ModelRenderConfig = ModelRenderConfig(1.5f, 0, 0),
        @SerializedName("skinPreview") val skinPreview: ModelRenderConfig = ModelRenderConfig(5.0f, 0, -80),
        @SerializedName("allSkinsGrid") val allSkinsGrid: ModelRenderConfig = ModelRenderConfig(1.5f, 0, 0)
    )

    var data = UiConfigData()
        private set

    fun load() {
        if (!configFile.exists()) {
            writeDefault()
        }
        try {
            data = GSON.fromJson(configFile.readText(), UiConfigData::class.java) ?: UiConfigData()
            CobblemonSkinMod.LOGGER.info("CobblemonSkin: loaded ui_config.json (partyCard=${data.partyCard}, skinPreview=${data.skinPreview}, allSkinsGrid=${data.allSkinsGrid})")
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.error("CobblemonSkin: failed to read ui_config.json — ${e.message}")
            data = UiConfigData()
        }
    }

    private fun writeDefault() {
        configFile.parentFile.mkdirs()
        configFile.writeText(GSON.toJson(UiConfigData()))
        CobblemonSkinMod.LOGGER.info("CobblemonSkin: created default ui_config.json at ${configFile.absolutePath}")
    }
}
