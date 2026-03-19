package com.example.cobblemon_skin.client

import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.PokemonGuiUtilsKt
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.Species
import com.example.cobblemon_skin.CobblemonSkinMod
import com.example.cobblemon_skin.config.UiConfig
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import org.joml.Quaternionf

@Environment(EnvType.CLIENT)
class SkinScreen(private val availableSkins: List<String>) : Screen(Component.literal("CobblemonSkin")) {

    private enum class Tab { PARTY, ALL_SKINS }

    // ── State ───────────────────────────────────────────────────────────────
    private var currentTab = Tab.PARTY
    private var selectedSlot = -1          // -1 = none selected
    private var selectedSkin: String? = null
    private var scrollOffset = 0
    private var partySkinIndex = 0       // current skin index for ◀▶ navigation

    // ── Search ────────────────────────────────────────────────────────────
    private var searchQuery = ""
    private var searchActive = false
    private var filteredSkins: List<String> = emptyList()
    private val speciesChineseCache = mutableMapOf<String, String>()

    // ── Skin set grouping ────────────────────────────────────────────────
    private var groupBySet = true
    private val expandedSets = mutableSetOf<String>()
    private var skinSetGroups: List<SkinSetGroup> = emptyList()
    private var groupScrollOffset = 0

    private data class SkinSetGroup(
        val setName: String,
        val displayName: String,
        val skins: List<String>
    )

    // ── Layout ──────────────────────────────────────────────────────────────
    private val panelW = 520
    private val panelH = 340
    private var px = 0
    private var py = 0
    private val titleBarH = 28
    private val divH = 1

    // ── Colours ─────────────────────────────────────────────────────────────
    private val cBg         = 0xFF0D1117.toInt()
    private val cTitleBar   = 0xFF161B22.toInt()
    private val cDiv        = 0xFF30363D.toInt()
    private val cAccent     = 0xFFF0883E.toInt()
    private val cGreen      = 0xFF238636.toInt()
    private val cBtnGray    = 0xFF30363D.toInt()
    private val cCardBg     = 0xFF161B22.toInt()
    private val cCardSel    = 0xFF1A2744.toInt()
    private val cCardHover  = 0xFF1C2333.toInt()
    private val cPreviewBg  = 0xFF0A0E14.toInt()
    private val cPreviewBdr = 0xFF1C2333.toInt()
    private val cIconBg     = 0xFF1C2333.toInt()
    private val cIconBgSel  = 0xFF162040.toInt()
    private val cWhite      = 0xFFFFFFFF.toInt()
    private val cTextPri    = 0xFFC9D1D9.toInt()
    private val cTextSec    = 0xFF8B949E.toInt()
    private val cTextDim    = 0xFF484F58.toInt()

    // ── Render states ───────────────────────────────────────────────────────
    private val partyStates = Array(6) { FloatingState() }
    private val skinPreviewStates = mutableMapOf<String, FloatingState>()
    private var cachedPartyRenderables = arrayOfNulls<RenderablePokemon>(6)
    private val cachedSkinRenderables = mutableMapOf<String, RenderablePokemon?>()

    // Cached reflection method for species lookup (avoid repeated reflection)
    companion object {
        private var cachedSpeciesMethod: java.lang.reflect.Method? = null
        private val asyncExecutor = java.util.concurrent.Executors.newSingleThreadExecutor { r ->
            Thread(r, "CobblemonSkin-IO").apply { isDaemon = true }
        }
    }

    private val setNameTranslations = mapOf(
        "crystal" to "水晶", "crimson" to "绯红", "cybernetic" to "赛博",
        "halloween" to "万圣节", "lunar" to "新春", "maelstrom" to "漩涡",
        "pirate" to "海盗", "blossom" to "花开", "spring" to "春日",
        "skeleton" to "骷髅", "divine" to "神圣", "prehistoric2" to "远古",
        "prehistoric" to "远古", "valentines" to "情人节", "rgb" to "RGB",
        "molten" to "熔岩", "lightning" to "闪电", "mega" to "超级进化",
        "delta" to "三角洲", "shadow2" to "暗影", "shadow" to "暗影",
        "pinkan" to "粉色", "easter" to "复活节", "newyear" to "新年",
        "glasses" to "眼镜", "jump" to "鲤鱼王跳跃", "cafe" to "咖啡馆", "amethyst" to "紫晶",
        "arcade" to "街机",
        "christmas" to "圣诞", "coral" to "珊瑚", "cottage" to "田园",
        "dessert" to "甜点", "enchanted" to "魔法", "floral" to "花卉",
        "galactic" to "银河", "glacier" to "冰川", "golden" to "黄金",
        "harvest" to "丰收", "infernal" to "地狱", "jade" to "翡翠",
        "neon" to "霓虹", "noble" to "贵族", "obsidian" to "黑曜石",
        "phantom" to "幽灵", "prismatic" to "棱镜", "royal" to "皇家",
        "rustic" to "乡野", "sakura" to "樱花", "sapphire" to "蓝宝石",
        "steampunk" to "蒸汽朋克", "storm" to "风暴", "toxic" to "毒素",
        "tropical" to "热带", "volcanic" to "火山", "winter" to "冬季",
        "aquatic" to "水族", "celestial" to "天界", "ember" to "余烬",
        "altered" to "异变形态", "goth" to "哥特", "gunslinger" to "枪手",
        "coterian" to "联盟", "paldea" to "帕底亚", "clone" to "克隆",
        "babylegends" to "幼年传说"
    )

    private fun extractSetName(skinId: String): String {
        val speciesStr = CobblemonSkinMod.skinSpeciesMap[skinId]
        val speciesName = speciesStr?.substringAfter(":") ?: ""

        // Remove species prefix to get the set part
        var suffix = skinId
        if (speciesName.isNotEmpty() && skinId.startsWith(speciesName)) {
            suffix = skinId.removePrefix(speciesName).trimStart('_')
        }
        if (suffix.isEmpty()) suffix = skinId

        // For hyphenated suffixes like "jump-purple-patches", use the first segment
        val baseSuffix = if (suffix.contains("-")) suffix.substringBefore("-") else suffix

        // Try known set name matching on baseSuffix and full suffix
        for (setName in setNameTranslations.keys.sortedByDescending { it.length }) {
            if (baseSuffix == setName || suffix.startsWith(setName)) return setName
        }

        // Match against known names in the full skinId
        for (setName in setNameTranslations.keys.sortedByDescending { it.length }) {
            if (skinId.contains("_${setName}_") || skinId.endsWith("_$setName")) return setName
        }

        // Fallback: use baseSuffix (first segment of the last part)
        val parts = suffix.split("_")
        return parts.firstOrNull()?.let {
            if (it.contains("-")) it.substringBefore("-") else it
        } ?: "其他"
    }

    private fun buildSkinSetGroups() {
        val rawGroups = linkedMapOf<String, MutableList<String>>()
        for (skinId in availableSkins) {
            val setName = extractSetName(skinId)
            rawGroups.getOrPut(setName) { mutableListOf() }.add(skinId)
        }

        // Merge tiny groups (1-2 skins) into "其他" unless they have a translation
        val merged = linkedMapOf<String, MutableList<String>>()
        for ((setName, skins) in rawGroups) {
            if (skins.size <= 2 && setName !in setNameTranslations) {
                merged.getOrPut("其他") { mutableListOf() }.addAll(skins)
            } else {
                merged.getOrPut(setName) { mutableListOf() }.addAll(skins)
            }
        }

        skinSetGroups = merged.map { (setName, skins) ->
            val displayName = setNameTranslations[setName] ?: setName
            SkinSetGroup(setName, displayName, skins.sorted())
        }.sortedByDescending { it.skins.size }
    }

    override fun init() {
        super.init()
        px = (width - panelW) / 2
        py = (height - panelH) / 2
        cachedSkinRenderables.clear()
        filteredSkins = availableSkins
        buildSkinSetGroups()
        // Async: reload configs off the render thread
        asyncExecutor.submit {
            try {
                UiConfig.load()
                val parsed = parseSkinPackConfigs()
                minecraft?.execute {
                    CobblemonSkinMod.skinUiConfigs.clear()
                    CobblemonSkinMod.skinUiConfigs.putAll(parsed.uiConfigs)
                    CobblemonSkinMod.skinMetaMap.putAll(parsed.metaMap)
                }
            } catch (_: Exception) {}
        }
    }

    private data class ParsedSkinConfigs(
        val uiConfigs: Map<String, CobblemonSkinMod.SkinUiConfig>,
        val metaMap: Map<String, CobblemonSkinMod.SkinMeta>
    )

    private fun parseSkinPackConfigs(): ParsedSkinConfigs {
        val uiResult = mutableMapOf<String, CobblemonSkinMod.SkinUiConfig>()
        val metaResult = mutableMapOf<String, CobblemonSkinMod.SkinMeta>()
        val skinsDir = com.example.cobblemon_skin.loader.SkinPackLoader.skinsDir
        if (!skinsDir.exists()) return ParsedSkinConfigs(uiResult, metaResult)
        val packDirs = skinsDir.listFiles()?.filter { it.isDirectory } ?: return ParsedSkinConfigs(uiResult, metaResult)
        for (packDir in packDirs) {
            val mainYml = java.io.File(packDir, "main.yml")
            if (!mainYml.exists()) continue
            val skinId = packDir.name
            val text = mainYml.readText()
            var uiScale = 1.0f; var uiOx = 0; var uiOy = 0
            var desc = ""; var quality = "普通"; var obtain = ""; var detail = ""
            var inUi = false; var inInfo = false
            for (rawLine in text.lines()) {
                val line = rawLine.trim()
                if (line.isEmpty() || line.startsWith("#")) continue
                if (line.endsWith(":") && !line.startsWith("-")) {
                    val key = line.removeSuffix(":").trim()
                    inUi = key == "ui"; inInfo = key == "info"; continue
                }
                if (inUi && line.contains(":")) {
                    val k = line.substringBefore(":").trim()
                    val v = line.substringAfter(":").trim()
                    when (k) {
                        "scale" -> uiScale = v.toFloatOrNull() ?: 1.0f
                        "offsetX" -> uiOx = v.toIntOrNull() ?: 0
                        "offsetY" -> uiOy = v.toIntOrNull() ?: 0
                    }
                }
                if (inInfo && line.contains(":")) {
                    val k = line.substringBefore(":").trim()
                    val v = line.substringAfter(":").trim().removeSurrounding("\"").removeSurrounding("'")
                    when (k) {
                        "description" -> desc = v
                        "quality" -> quality = v
                        "obtain" -> obtain = v
                        "detail" -> detail = v
                    }
                }
            }
            if (uiScale != 1.0f || uiOx != 0 || uiOy != 0) {
                uiResult[skinId] = CobblemonSkinMod.SkinUiConfig(uiScale, uiOx, uiOy)
            }
            metaResult[skinId] = CobblemonSkinMod.SkinMeta(desc, quality, obtain, detail)
        }
        return ParsedSkinConfigs(uiResult, metaResult)
    }

    private var cacheInvalidateTicksLeft = 0
    private var lastInvalidateFrame = 0L
    private var frameCounter = 0L

    private fun applySkin(skinId: String) {
        if (selectedSlot < 0) return
        SkinClientMod.requestApplySkin(selectedSlot + 1, skinId)
        invalidateCaches()
    }

    private fun clearSkin() {
        if (selectedSlot < 0) return
        SkinClientMod.requestClearSkin(selectedSlot + 1)
        invalidateCaches()
    }

    private fun invalidateCaches() {
        // Only invalidate the selected slot's renderable + matching skin renderables
        if (selectedSlot in 0..5) {
            cachedPartyRenderables[selectedSlot] = null
        }
        cachedSkinRenderables.clear()
        cacheInvalidateTicksLeft = 10
        lastInvalidateFrame = frameCounter
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RENDER
    // ═════════════════════════════════════════════════════════════════════════
    override fun renderBackground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        graphics.fill(0, 0, width, height, 0x80000000.toInt())
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        frameCounter++
        // Re-invalidate periodically to catch server sync, only every 2 frames
        if (cacheInvalidateTicksLeft > 0 && (frameCounter - lastInvalidateFrame) % 2 == 0L) {
            cacheInvalidateTicksLeft--
            if (selectedSlot in 0..5) cachedPartyRenderables[selectedSlot] = null
            cachedSkinRenderables.clear()
        }
        renderBackground(graphics, mouseX, mouseY, delta)
        graphics.fill(px, py, px + panelW, py + panelH, cBg)

        renderTitleBar(graphics, mouseX, mouseY)
        graphics.fill(px, py + titleBarH, px + panelW, py + titleBarH + divH, cDiv)

        val contentY = py + titleBarH + divH
        val contentH = panelH - titleBarH - divH

        when (currentTab) {
            Tab.PARTY -> renderPartyTab(graphics, px, contentY, contentH, mouseX, mouseY, delta)
            Tab.ALL_SKINS -> renderAllSkinsTab(graphics, px, contentY, contentH, mouseX, mouseY, delta)
        }

        super.render(graphics, mouseX, mouseY, delta)
    }

    private fun renderTitleBar(graphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val x = px
        val y = py
        graphics.fill(x, y, x + panelW, y + titleBarH, cTitleBar)

        // Logo
        graphics.drawString(font, "§6CobblemonSkin", x + 12, y + 10, cAccent, false)

        // Tabs centered
        val tabW1 = 40
        val tabW2 = 56
        val tabGap = 0
        val totalTabW = tabW1 + tabW2 + tabGap
        val tabX = x + (panelW - totalTabW) / 2

        // Tab: 队伍
        val tab1Bg = if (currentTab == Tab.PARTY) cBg else cTitleBar
        val tab1Color = if (currentTab == Tab.PARTY) cAccent else cTextSec
        graphics.fill(tabX, y + 4, tabX + tabW1, y + titleBarH, tab1Bg)
        graphics.drawCenteredString(font, "队伍", tabX + tabW1 / 2, y + 10, tab1Color)

        // Tab: 全部皮肤
        val tab2X = tabX + tabW1 + tabGap
        val tab2Bg = if (currentTab == Tab.ALL_SKINS) cBg else cTitleBar
        val tab2Color = if (currentTab == Tab.ALL_SKINS) cAccent else cTextSec
        graphics.fill(tab2X, y + 4, tab2X + tabW2, y + titleBarH, tab2Bg)
        graphics.drawCenteredString(font, "全部皮肤", tab2X + tabW2 / 2, y + 10, tab2Color)

        // Close button
        graphics.drawString(font, "§7✕", x + panelW - 18, y + 10, cTextSec, false)
    }

    // ─── PARTY TAB ──────────────────────────────────────────────────────────
    // 3-column layout: 15% party | 70% preview | 15% info
    private val partyLeftW = 78
    private val infoRightW = 78

    private fun renderPartyTab(graphics: GuiGraphics, x0: Int, y0: Int, h: Int, mouseX: Int, mouseY: Int, delta: Float) {
        val padY = 6
        val party = CobblemonClient.INSTANCE.getStorage().party

        // ══ Left column: vertical party list (15%) ══
        val slotW = partyLeftW - 8
        val slotH = 44
        val slotGap = 3
        val slotX = x0 + 4

        for (idx in 0..5) {
            val cy = y0 + padY + idx * (slotH + slotGap)
            val isSelected = idx == selectedSlot
            val hovered = mouseX in slotX..(slotX + slotW) && mouseY in cy..(cy + slotH)
            val bg = when {
                isSelected -> cCardSel
                hovered    -> cCardHover
                else       -> cCardBg
            }
            graphics.fill(slotX, cy, slotX + slotW, cy + slotH, bg)
            if (isSelected) drawBorder(graphics, slotX, cy, slotW, slotH, cAccent, 2)

            val pokemon = party.get(idx)
            if (pokemon != null) {
                if (cachedPartyRenderables[idx] == null) {
                    cachedPartyRenderables[idx] = pokemon.asRenderablePokemon()
                }
                val renderable = cachedPartyRenderables[idx]!!
                val pc = UiConfig.data.partyCard
                graphics.enableScissor(slotX, cy, slotX + slotW, cy + 26)
                renderMiniPokemon(graphics, renderable, slotX + slotW / 2 + pc.offsetX, cy + 2 + pc.offsetY, delta, partyStates[idx], pc.scale * 0.8f)
                graphics.disableScissor()

                // Chinese species name (via Minecraft translation)
                val cnName = try { pokemon.species.getTranslatedName().getString() } catch (_: Exception) { pokemon.species.name }
                val level = try { pokemon.getLevel() } catch (_: Exception) { 0 }
                val truncName = if (font.width(cnName) > slotW - 4) {
                    var s = cnName; while (font.width("$s..") > slotW - 4 && s.length > 1) s = s.dropLast(1); "$s.."
                } else cnName
                graphics.drawCenteredString(font, truncName, slotX + slotW / 2, cy + 27, cTextPri)

                // Level + skin indicator
                val activeSkin = CobblemonSkinMod.getActiveSkin(pokemon)
                val lvText = if (activeSkin != null) "Lv.$level §6✦" else "§7Lv.$level"
                graphics.drawCenteredString(font, lvText, slotX + slotW / 2, cy + 37, cTextSec)
            } else {
                cachedPartyRenderables[idx] = null
                graphics.drawCenteredString(font, "§8-", slotX + slotW / 2, cy + slotH / 2 - 4, cTextDim)
            }
        }

        // ══ Divider left ══
        val divLX = x0 + partyLeftW
        graphics.fill(divLX, y0, divLX + 1, y0 + h, cDiv)

        // ══ Divider right ══
        val divRX = x0 + panelW - infoRightW
        graphics.fill(divRX, y0, divRX + 1, y0 + h, cDiv)

        // ══ Center: large model preview (70%) ══
        val centerX = divLX + 1
        val centerW = divRX - centerX
        val centerMidX = centerX + centerW / 2

        // Dark preview background
        graphics.fill(centerX, y0, divRX, y0 + h, cPreviewBg)

        if (selectedSlot < 0) {
            graphics.drawCenteredString(font, "§7← 选择宝可梦", centerMidX, y0 + h / 2 - 4, cTextDim)
            // Right panel placeholder
            renderInfoPanelEmpty(graphics, divRX + 1, y0, infoRightW, h)
            return
        }

        val pokemon = party.get(selectedSlot)
        if (pokemon == null) {
            graphics.drawCenteredString(font, "§7该槽位无精灵", centerMidX, y0 + h / 2 - 4, cTextDim)
            renderInfoPanelEmpty(graphics, divRX + 1, y0, infoRightW, h)
            return
        }

        val speciesId = pokemon.species.resourceIdentifier.toString()
        val matchingSkins = availableSkins.filter { CobblemonSkinMod.skinSpeciesMap[it] == speciesId }

        if (matchingSkins.isEmpty()) {
            graphics.drawCenteredString(font, "§7暂无可用皮肤", centerMidX, y0 + h / 2 - 4, cTextDim)
            renderInfoPanelEmpty(graphics, divRX + 1, y0, infoRightW, h)
            return
        }

        // Clamp skin index
        partySkinIndex = partySkinIndex.coerceIn(0, matchingSkins.size - 1)
        val skinId = matchingSkins[partySkinIndex]

        // Skin name at top
        graphics.drawCenteredString(font, "§6$skinId", centerMidX, y0 + 8, cAccent)

        // Large model preview
        val prevSize = minOf(centerW - 40, h - 90)
        val prevX = centerMidX - prevSize / 2
        val prevY = y0 + 24
        graphics.fill(prevX, prevY, prevX + prevSize, prevY + prevSize, 0xFF0D1117.toInt())
        drawBorder(graphics, prevX, prevY, prevSize, prevSize, 0xFF1C2333.toInt(), 1)

        // Use the SELECTED party Pokémon directly + skin aspect (not generic search)
        val cacheKey = "party_${selectedSlot}_$skinId"
        val renderable = cachedSkinRenderables.getOrPut(cacheKey) {
            val base = pokemon.asRenderablePokemon()
            val aspects = base.aspects
                .filter { !it.startsWith(CobblemonSkinMod.ASPECT_PREFIX) }
                .toSet() + setOf(CobblemonSkinMod.aspectFor(skinId))
            RenderablePokemon(base.species, aspects, base.heldItem)
        }
        if (renderable != null) {
            val state = skinPreviewStates.getOrPut(cacheKey) { FloatingState() }
            val uiCfg = CobblemonSkinMod.skinUiConfigs[skinId]
            val sp = UiConfig.data.skinPreview
            val cfgScale = (uiCfg?.scale ?: 1.0f)
            val cfgOx = (uiCfg?.offsetX ?: 0)
            val cfgOy = (uiCfg?.offsetY ?: 0)
            graphics.enableScissor(prevX, prevY, prevX + prevSize, prevY + prevSize)
            renderMiniPokemon(graphics, renderable, prevX + prevSize / 2 + sp.offsetX + cfgOx, prevY + prevSize - 55 + sp.offsetY + cfgOy, delta, state, sp.scale * cfgScale)
            graphics.disableScissor()
        }

        // Navigation: ◀ 1/N ▶
        val navY = prevY + prevSize + 6
        val navBtnW = 22
        val navBtnH = 18
        val prevBtnX = centerMidX - 40
        val nextBtnX = centerMidX + 18
        val prevHov = mouseX in prevBtnX..(prevBtnX + navBtnW) && mouseY in navY..(navY + navBtnH)
        val nextHov = mouseX in nextBtnX..(nextBtnX + navBtnW) && mouseY in navY..(navY + navBtnH)
        graphics.fill(prevBtnX, navY, prevBtnX + navBtnW, navY + navBtnH, if (prevHov) cCardHover else cCardBg)
        graphics.fill(nextBtnX, navY, nextBtnX + navBtnW, navY + navBtnH, if (nextHov) cCardHover else cCardBg)
        graphics.drawCenteredString(font, "◀", prevBtnX + navBtnW / 2, navY + 5, cTextSec)
        graphics.drawCenteredString(font, "▶", nextBtnX + navBtnW / 2, navY + 5, cTextSec)
        graphics.drawCenteredString(font, "${partySkinIndex + 1} / ${matchingSkins.size}", centerMidX, navY + 5, cTextSec)

        // Action buttons: 应用 / 清除
        val btnY = navY + navBtnH + 6
        val applyBtnX = centerMidX - 68
        val clearBtnX = centerMidX + 4
        drawButton(graphics, applyBtnX, btnY, 64, 22, "应用", cGreen, cWhite, mouseX, mouseY)
        drawButton(graphics, clearBtnX, btnY, 56, 22, "清除", cBtnGray, cTextPri, mouseX, mouseY)

        // Equipped indicator
        val activeSkin = CobblemonSkinMod.getActiveSkin(pokemon)
        if (activeSkin == skinId) {
            graphics.drawCenteredString(font, "§a✓ 已装备", centerMidX, btnY + 26, cGreen)
        }

        // ══ Right column: info panel (15%) ══
        renderInfoPanel(graphics, divRX + 1, y0, infoRightW, h, skinId)
    }

    private fun renderInfoPanelEmpty(graphics: GuiGraphics, x: Int, y: Int, w: Int, h: Int) {
        graphics.fill(x, y, x + w, y + h, cBg)
    }

    private fun renderInfoPanel(graphics: GuiGraphics, x: Int, y: Int, w: Int, h: Int, skinId: String) {
        graphics.fill(x, y, x + w, y + h, cBg)
        val meta = CobblemonSkinMod.skinMetaMap[skinId]
        val padX = 6
        val padY = 8
        val ix = x + padX
        val maxW = w - padX * 2

        // Title
        graphics.drawString(font, "§6皮肤介绍", ix, y + padY, cAccent, false)
        graphics.fill(ix, y + padY + 12, x + w - padX, y + padY + 13, cDiv)

        // Quality
        var rowY = y + padY + 18
        graphics.drawString(font, "§8品质", ix, rowY, cTextDim, false)
        rowY += 11
        val qualityText = meta?.quality ?: "普通"
        val qualityColor = getQualityColor(qualityText)
        graphics.drawString(font, qualityText, ix, rowY, qualityColor, false)
        rowY += 14
        graphics.fill(ix, rowY, x + w - padX, rowY + 1, 0xFF1C2333.toInt())
        rowY += 5

        // Obtain
        graphics.drawString(font, "§8获取途径", ix, rowY, cTextDim, false)
        rowY += 11
        val obtainText = meta?.obtain ?: ""
        if (obtainText.isNotEmpty()) {
            graphics.drawString(font, "§7$obtainText", ix, rowY, cTextSec, false)
        } else {
            graphics.drawString(font, "§8-", ix, rowY, cTextDim, false)
        }
        rowY += 14
        graphics.fill(ix, rowY, x + w - padX, rowY + 1, 0xFF1C2333.toInt())
        rowY += 5

        // Description / Detail (word-wrapped)
        graphics.drawString(font, "§8描述", ix, rowY, cTextDim, false)
        rowY += 11
        val desc = meta?.detail?.ifEmpty { meta.description } ?: meta?.description ?: ""
        if (desc.isNotEmpty()) {
            graphics.enableScissor(ix, rowY, x + w - padX, y + h - padY)
            var remaining = desc
            while (remaining.isNotEmpty() && rowY + 10 < y + h - padY) {
                var end = remaining.length
                while (end > 0 && font.width(remaining.substring(0, end)) > maxW) end--
                if (end == 0) end = 1
                graphics.drawString(font, "§7${remaining.substring(0, end)}", ix, rowY, cTextSec, false)
                remaining = remaining.substring(end)
                rowY += 10
            }
            graphics.disableScissor()
        } else {
            graphics.drawString(font, "§8-", ix, rowY, cTextDim, false)
        }
    }

    // ─── ALL SKINS TAB ──────────────────────────────────────────────────────
    private fun renderAllSkinsTab(graphics: GuiGraphics, x0: Int, y0: Int, h: Int, mouseX: Int, mouseY: Int, delta: Float) {
        if (groupBySet) {
            renderGroupedSkinsTab(graphics, x0, y0, h, mouseX, mouseY, delta)
        } else {
            renderFlatSkinsTab(graphics, x0, y0, h, mouseX, mouseY, delta)
        }
    }

    private fun renderSearchBar(graphics: GuiGraphics, x0: Int, y: Int) {
        val padX = 10
        val searchW = 160
        val searchH = 14
        val sx = x0 + padX
        val bg = if (searchActive) 0xFF1A2744.toInt() else cCardBg
        graphics.fill(sx, y, sx + searchW, y + searchH, bg)
        drawBorder(graphics, sx, y, searchW, searchH, if (searchActive) cAccent else cDiv, 1)
        val displayText = if (searchQuery.isEmpty() && !searchActive) "§8🔍 搜索宝可梦..." else "§f$searchQuery§7|"
        graphics.drawString(font, displayText, sx + 3, y + 3, cTextPri, false)

        // Count + toggle
        val countText = "§7(${filteredSkins.size}/${availableSkins.size})"
        graphics.drawString(font, countText, sx + searchW + 6, y + 3, cTextSec, false)
        val toggleText = if (groupBySet) "§6平铺 ▸" else "§6分组 ▸"
        graphics.drawString(font, toggleText, x0 + panelW - padX - font.width(if (groupBySet) "平铺 ▸" else "分组 ▸"), y + 3, cAccent, false)
    }

    private fun renderFlatSkinsTab(graphics: GuiGraphics, x0: Int, y0: Int, h: Int, mouseX: Int, mouseY: Int, delta: Float) {
        val padX = 10
        val padY = 6
        val cardGap = 6
        val cols = 4
        val cardW = (panelW - padX * 2 - cardGap * (cols - 1)) / cols
        val cardH = 130
        val subBarH = 20
        val gridY = y0 + padY + subBarH

        renderSearchBar(graphics, x0, y0 + padY)

        val maxRows = (h - padY - subBarH - padY) / (cardH + cardGap)
        val startIdx = scrollOffset * cols
        val visible = filteredSkins.drop(startIdx).take(maxRows * cols)

        val party = CobblemonClient.INSTANCE.getStorage().party

        for ((idx, skinId) in visible.withIndex()) {
            val col = idx % cols
            val row = idx / cols
            val cx = x0 + padX + col * (cardW + cardGap)
            val cy = gridY + row * (cardH + cardGap)

            renderSkinCard(graphics, cx, cy, cardW, cardH, skinId, party, mouseX, mouseY, delta)
        }

        // Scroll indicator
        val totalRows = (filteredSkins.size + cols - 1) / cols
        if (totalRows > maxRows) {
            val scrollText = "§7↑↓ 滚动查看更多 (${scrollOffset + 1}/$totalRows)"
            graphics.drawCenteredString(font, scrollText, x0 + panelW / 2, y0 + h - 14, cTextDim)
        }
    }

    private fun renderGroupedSkinsTab(graphics: GuiGraphics, x0: Int, y0: Int, h: Int, mouseX: Int, mouseY: Int, delta: Float) {
        val padX = 10
        val padY = 6
        val subBarH = 20
        val headerH = 22
        val cardGap = 5
        val cols = 4
        val cardW = (panelW - padX * 2 - cardGap * (cols - 1)) / cols
        val cardH = 100
        val groupGap = 4

        renderSearchBar(graphics, x0, y0 + padY)

        val contentTop = y0 + padY + subBarH
        val contentBottom = y0 + h - 14
        graphics.enableScissor(x0, contentTop, x0 + panelW, contentBottom)

        val party = CobblemonClient.INSTANCE.getStorage().party
        var curY = contentTop - groupScrollOffset

        for (group in skinSetGroups) {
            val isExpanded = group.setName in expandedSets

            // Group header
            if (curY + headerH > contentTop - headerH && curY < contentBottom) {
                val headerHovered = mouseX in (x0 + padX)..(x0 + panelW - padX) && mouseY in curY..(curY + headerH)
                val headerBg = if (headerHovered) cCardHover else cTitleBar
                graphics.fill(x0 + padX, curY, x0 + panelW - padX, curY + headerH, headerBg)

                val arrow = if (isExpanded) "§6▼" else "§8▶"
                graphics.drawString(font, arrow, x0 + padX + 6, curY + 7, cAccent, false)
                graphics.drawString(font, "§f${group.displayName}", x0 + padX + 20, curY + 7, cTextPri, false)
                graphics.drawString(font, "§7(${group.skins.size})", x0 + padX + 22 + font.width(group.displayName), curY + 7, cTextSec, false)
            }
            curY += headerH + 2

            // Expanded skin cards
            if (isExpanded) {
                val rows = (group.skins.size + cols - 1) / cols
                for (row in 0 until rows) {
                    if (curY + cardH > contentTop - cardH && curY < contentBottom) {
                        for (col in 0 until cols) {
                            val skinIdx = row * cols + col
                            if (skinIdx >= group.skins.size) break
                            val skinId = group.skins[skinIdx]
                            val cx = x0 + padX + col * (cardW + cardGap)
                            renderSkinCard(graphics, cx, curY, cardW, cardH, skinId, party, mouseX, mouseY, delta)
                        }
                    }
                    curY += cardH + cardGap
                }
            }
            curY += groupGap
        }

        graphics.disableScissor()

        // Scroll indicator
        graphics.drawCenteredString(font, "§8↑↓ 滚动查看更多套装", x0 + panelW / 2, y0 + h - 12, cTextDim)
    }

    private fun renderSkinCard(
        graphics: GuiGraphics, cx: Int, cy: Int, cardW: Int, cardH: Int,
        skinId: String, party: com.cobblemon.mod.common.client.storage.ClientParty,
        mouseX: Int, mouseY: Int, delta: Float
    ) {
        val isSelected = skinId == selectedSkin
        val hovered = mouseX in cx..(cx + cardW) && mouseY in cy..(cy + cardH)
        val bg = when {
            isSelected -> cCardSel
            hovered -> cCardHover
            else -> cCardBg
        }
        graphics.fill(cx, cy, cx + cardW, cy + cardH, bg)
        if (isSelected) drawBorder(graphics, cx, cy, cardW, cardH, cAccent, 1)

        // Preview area
        val prevPad = 4
        val prevH = cardH - 36
        graphics.fill(cx + prevPad, cy + prevPad, cx + cardW - prevPad, cy + prevPad + prevH, cPreviewBg)

        val renderable = getOrCreateSkinRenderable(skinId)
        if (renderable != null) {
            val state = skinPreviewStates.getOrPut(skinId) { FloatingState() }
            val uiCfg = CobblemonSkinMod.skinUiConfigs[skinId]
            val ag = UiConfig.data.allSkinsGrid
            val cfgScale = (uiCfg?.scale ?: 1.0f)
            val cfgOx = (uiCfg?.offsetX ?: 0)
            val cfgOy = (uiCfg?.offsetY ?: 0)
            val prevCenterX = cx + cardW / 2 + ag.offsetX + cfgOx
            val prevBottomY = cy + prevPad + prevH - 47 + ag.offsetY + cfgOy
            graphics.enableScissor(cx + prevPad, cy + prevPad, cx + cardW - prevPad, cy + prevPad + prevH)
            renderMiniPokemon(graphics, renderable, prevCenterX, prevBottomY, delta, state, ag.scale * cfgScale)
            graphics.disableScissor()
        }

        // Name
        val nameColor = if (isSelected) cAccent else cTextPri
        val maxNameW = cardW - 4
        val displayName = if (font.width(skinId) > maxNameW) {
            var s = skinId; while (font.width("$s..") > maxNameW && s.length > 1) s = s.dropLast(1); "$s.."
        } else skinId
        graphics.drawCenteredString(font, displayName, cx + cardW / 2, cy + prevPad + prevH + 4, nameColor)

        // Species
        val speciesName = resolveSpeciesName(CobblemonSkinMod.skinSpeciesMap[skinId])
        if (speciesName != null) {
            graphics.drawCenteredString(font, "§7$speciesName", cx + cardW / 2, cy + prevPad + prevH + 15, cTextSec)
        }

        // Equipped tag
        val isEquipped = isAnySkinEquipped(party, skinId)
        if (isEquipped) {
            val tagW = font.width("已装备") + 8
            val tagX = cx + (cardW - tagW) / 2
            val tagY = cy + cardH - 14
            graphics.fill(tagX, tagY, tagX + tagW, tagY + 11, cGreen)
            graphics.drawCenteredString(font, "已装备", cx + cardW / 2, tagY + 2, cWhite)
        }
    }

    private fun calcGroupedContentHeight(): Int {
        val headerH = 22
        val cardGap = 5
        val cols = 4
        val cardH = 100
        val groupGap = 4
        val contentH = panelH - titleBarH - divH
        var total = 0
        for (group in skinSetGroups) {
            total += headerH + 2
            if (group.setName in expandedSets) {
                val rows = (group.skins.size + cols - 1) / cols
                total += rows * (cardH + cardGap)
            }
            total += groupGap
        }
        return total - contentH + 40
    }

    // ─── HELPERS ────────────────────────────────────────────────────────────
    private fun drawBorder(graphics: GuiGraphics, x: Int, y: Int, w: Int, h: Int, color: Int, thickness: Int) {
        graphics.fill(x, y, x + w, y + thickness, color)
        graphics.fill(x, y + h - thickness, x + w, y + h, color)
        graphics.fill(x, y, x + thickness, y + h, color)
        graphics.fill(x + w - thickness, y, x + w, y + h, color)
    }

    private fun drawButton(graphics: GuiGraphics, x: Int, y: Int, w: Int, h: Int, text: String, bg: Int, fg: Int, mouseX: Int, mouseY: Int) {
        val hovered = mouseX in x..(x + w) && mouseY in y..(y + h)
        val col = if (hovered) brighten(bg) else bg
        graphics.fill(x, y, x + w, y + h, col)
        graphics.drawCenteredString(font, text, x + w / 2, y + (h - 8) / 2, fg)
    }

    private fun brighten(color: Int): Int {
        val a = (color ushr 24) and 0xFF
        val r = minOf(255, ((color ushr 16) and 0xFF) + 20)
        val g = minOf(255, ((color ushr 8) and 0xFF) + 20)
        val b = minOf(255, (color and 0xFF) + 20)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun getOrCreateSkinRenderable(skinId: String): RenderablePokemon? {
        if (cachedSkinRenderables.containsKey(skinId)) return cachedSkinRenderables[skinId]

        // Strategy 1: Find a matching party Pokémon and clone with skin aspect
        val speciesStr = CobblemonSkinMod.skinSpeciesMap[skinId]
        if (speciesStr != null) {
            val party = CobblemonClient.INSTANCE.getStorage().party
            for (i in 0..5) {
                val pokemon = party.get(i) ?: continue
                if (pokemon.species.resourceIdentifier.toString() == speciesStr) {
                    val base = pokemon.asRenderablePokemon()
                    val aspects = base.aspects
                        .filter { !it.startsWith(CobblemonSkinMod.ASPECT_PREFIX) }
                        .toSet() + setOf(CobblemonSkinMod.aspectFor(skinId))
                    val renderable = RenderablePokemon(base.species, aspects, base.heldItem)
                    cachedSkinRenderables[skinId] = renderable
                    return renderable
                }
            }
        }

        // Strategy 2: Create from species lookup
        if (speciesStr != null) {
            val speciesId = ResourceLocation.tryParse(speciesStr)
            if (speciesId != null) {
                try {
                    val pokemonSpecies = lookupSpecies(speciesId)
                    if (pokemonSpecies != null) {
                        val aspects = setOf(CobblemonSkinMod.aspectFor(skinId))
                        val renderable = RenderablePokemon(pokemonSpecies, aspects, ItemStack.EMPTY)
                        cachedSkinRenderables[skinId] = renderable
                        return renderable
                    }
                } catch (_: Exception) {}
            }
        }

        cachedSkinRenderables[skinId] = null
        return null
    }

    private fun lookupSpecies(id: ResourceLocation): Species? {
        return try {
            val instance = PokemonSpecies.INSTANCE
            val method = cachedSpeciesMethod ?: instance.javaClass.getMethod("getByIdentifier", ResourceLocation::class.java).also { cachedSpeciesMethod = it }
            method.invoke(instance, id) as? Species
        } catch (_: Exception) { null }
    }

    private fun resolveSpeciesName(speciesStr: String?): String? {
        if (speciesStr == null) return null
        // Return cached Chinese name if available
        speciesChineseCache[speciesStr]?.let { return it }
        val speciesId = ResourceLocation.tryParse(speciesStr) ?: return speciesStr
        try {
            val pokemonSpecies = lookupSpecies(speciesId)
            if (pokemonSpecies != null) {
                val cnName = try { pokemonSpecies.getTranslatedName().getString() } catch (_: Exception) { pokemonSpecies.name }
                speciesChineseCache[speciesStr] = cnName
                return cnName
            }
            return speciesStr.substringAfter(":")
        } catch (_: Exception) {
            return speciesStr.substringAfter(":")
        }
    }

    private fun updateFilteredSkins() {
        if (searchQuery.isEmpty()) {
            filteredSkins = availableSkins
        } else {
            val q = searchQuery.lowercase()
            filteredSkins = availableSkins.filter { skinId ->
                // Match skin ID
                if (skinId.lowercase().contains(q)) return@filter true
                // Match species name (Chinese or English)
                val speciesStr = CobblemonSkinMod.skinSpeciesMap[skinId] ?: return@filter false
                val cnName = resolveSpeciesName(speciesStr) ?: ""
                val enName = speciesStr.substringAfter(":")
                cnName.lowercase().contains(q) || enName.lowercase().contains(q)
            }
        }
        // Rebuild groups for filtered list
        buildSkinSetGroupsFrom(filteredSkins)
        scrollOffset = 0
        groupScrollOffset = 0
    }

    private fun buildSkinSetGroupsFrom(skins: List<String>) {
        val rawGroups = linkedMapOf<String, MutableList<String>>()
        for (skinId in skins) {
            val setName = extractSetName(skinId)
            rawGroups.getOrPut(setName) { mutableListOf() }.add(skinId)
        }
        val merged = linkedMapOf<String, MutableList<String>>()
        for ((setName, skinList) in rawGroups) {
            if (skinList.size <= 2 && setName !in setNameTranslations) {
                merged.getOrPut("其他") { mutableListOf() }.addAll(skinList)
            } else {
                merged.getOrPut(setName) { mutableListOf() }.addAll(skinList)
            }
        }
        skinSetGroups = merged.map { (setName, skinList) ->
            val displayName = setNameTranslations[setName] ?: setName
            SkinSetGroup(setName, displayName, skinList.sorted())
        }.sortedByDescending { it.skins.size }
    }

    private fun getQualityColor(quality: String): Int {
        return when (quality) {
            "传说", "legendary" -> 0xFFFFD700.toInt()
            "史诗", "epic" -> 0xFFBF00FF.toInt()
            "稀有", "rare" -> 0xFF3B82F6.toInt()
            "精良", "uncommon" -> 0xFF22C55E.toInt()
            else -> 0xFF8B949E.toInt() // 普通/common
        }
    }

    private fun isAnySkinEquipped(party: com.cobblemon.mod.common.client.storage.ClientParty, skinId: String): Boolean {
        for (i in 0..5) {
            val pokemon = party.get(i) ?: continue
            if (CobblemonSkinMod.getActiveSkin(pokemon) == skinId) return true
        }
        return false
    }

    private fun renderMiniPokemon(
        graphics: GuiGraphics, renderable: RenderablePokemon,
        cx: Int, cy: Int, partialTicks: Float, state: FloatingState, scale: Float
    ) {
        val matrices = graphics.pose()
        matrices.pushPose()
        matrices.translate(cx.toDouble(), cy.toDouble(), 0.0)
        matrices.scale(scale, scale, scale)
        val rotation = Quaternionf().rotationXYZ(
            Math.toRadians(13.0).toFloat(), Math.toRadians(35.0).toFloat(), 0f
        )
        try {
            PokemonGuiUtilsKt.drawProfilePokemon(
                renderable, matrices, rotation, PoseType.PROFILE,
                state, partialTicks, 12f, true, false,
                1f, 1f, 1f, 1f, 0f, 0f
            )
        } catch (_: Exception) {}
        matrices.popPose()
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  INPUT
    // ═════════════════════════════════════════════════════════════════════════
    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val mx = mouseX.toInt()
        val my = mouseY.toInt()

        // Tab clicks
        val tabW1 = 40
        val tabW2 = 56
        val totalTabW = tabW1 + tabW2
        val tabX = px + (panelW - totalTabW) / 2
        if (my in py..(py + titleBarH)) {
            if (mx in tabX..(tabX + tabW1)) { currentTab = Tab.PARTY; scrollOffset = 0; return true }
            if (mx in (tabX + tabW1)..(tabX + totalTabW)) { currentTab = Tab.ALL_SKINS; scrollOffset = 0; return true }
            // Close button
            if (mx in (px + panelW - 22)..(px + panelW)) { onClose(); return true }
        }

        val contentY = py + titleBarH + divH
        val contentH = panelH - titleBarH - divH

        when (currentTab) {
            Tab.PARTY -> return handlePartyClick(mx, my, contentY, contentH)
            Tab.ALL_SKINS -> return handleAllSkinsClick(mx, my, contentY, contentH)
        }
    }

    private fun handlePartyClick(mx: Int, my: Int, contentY: Int, contentH: Int): Boolean {
        val padY = 6
        val slotW = partyLeftW - 8
        val slotH = 44
        val slotGap = 3
        val slotX = px + 4

        // Left column: party slot clicks
        for (idx in 0..5) {
            val cy = contentY + padY + idx * (slotH + slotGap)
            if (mx in slotX..(slotX + slotW) && my in cy..(cy + slotH)) {
                selectedSlot = if (selectedSlot == idx) -1 else idx
                selectedSkin = null
                partySkinIndex = 0
                return true
            }
        }

        // Center panel: nav + action buttons
        if (selectedSlot >= 0) {
            val party = CobblemonClient.INSTANCE.getStorage().party
            val pokemon = party.get(selectedSlot) ?: return super.mouseClicked(mx.toDouble(), my.toDouble(), 0)
            val speciesId = pokemon.species.resourceIdentifier.toString()
            val matchingSkins = availableSkins.filter { CobblemonSkinMod.skinSpeciesMap[it] == speciesId }
            if (matchingSkins.isEmpty()) return super.mouseClicked(mx.toDouble(), my.toDouble(), 0)

            val divLX = px + partyLeftW
            val divRX = px + panelW - infoRightW
            val centerX = divLX + 1
            val centerW = divRX - centerX
            val centerMidX = centerX + centerW / 2

            val prevSize = minOf(centerW - 40, contentH - 90)
            val prevY = contentY + 24
            val navY = prevY + prevSize + 6
            val navBtnW = 22
            val navBtnH = 18

            // ◀ button
            val prevBtnX = centerMidX - 40
            if (mx in prevBtnX..(prevBtnX + navBtnW) && my in navY..(navY + navBtnH)) {
                partySkinIndex = (partySkinIndex - 1).coerceAtLeast(0)
                return true
            }
            // ▶ button
            val nextBtnX = centerMidX + 18
            if (mx in nextBtnX..(nextBtnX + navBtnW) && my in navY..(navY + navBtnH)) {
                partySkinIndex = (partySkinIndex + 1).coerceAtMost(matchingSkins.size - 1)
                return true
            }

            // Apply button
            val btnY = navY + navBtnH + 6
            val applyBtnX = centerMidX - 68
            if (mx in applyBtnX..(applyBtnX + 64) && my in btnY..(btnY + 22)) {
                val skinId = matchingSkins[partySkinIndex.coerceIn(0, matchingSkins.size - 1)]
                applySkin(skinId)
                return true
            }
            // Clear button
            val clearBtnX = centerMidX + 4
            if (mx in clearBtnX..(clearBtnX + 56) && my in btnY..(btnY + 22)) {
                clearSkin()
                return true
            }
        }

        return super.mouseClicked(mx.toDouble(), my.toDouble(), 0)
    }

    private fun handleAllSkinsClick(mx: Int, my: Int, contentY: Int, contentH: Int): Boolean {
        val padX = 10
        val padY = 6
        val subBarH = 20

        // Search bar click
        val searchW = 160
        val searchH = 14
        val sx = px + padX
        val sy = contentY + padY
        if (mx in sx..(sx + searchW) && my in sy..(sy + searchH)) {
            searchActive = true
            return true
        } else if (searchActive && (my < sy || my > sy + searchH)) {
            searchActive = false
        }

        // Toggle group/flat view button
        val toggleTextW = if (groupBySet) font.width("平铺 ▸") else font.width("分组 ▸")
        val toggleX = px + panelW - padX - toggleTextW
        if (mx in toggleX..(toggleX + toggleTextW) && my in sy..(sy + 14)) {
            groupBySet = !groupBySet
            scrollOffset = 0
            groupScrollOffset = 0
            return true
        }

        if (groupBySet) {
            return handleGroupedSkinsClick(mx, my, contentY, contentH)
        } else {
            return handleFlatSkinsClick(mx, my, contentY, contentH)
        }
    }

    private fun handleFlatSkinsClick(mx: Int, my: Int, contentY: Int, contentH: Int): Boolean {
        val padX = 10
        val padY = 6
        val cardGap = 6
        val cols = 4
        val cardW = (panelW - padX * 2 - cardGap * (cols - 1)) / cols
        val cardH = 130
        val subBarH = 20
        val gridY = contentY + padY + subBarH

        val maxRows = (contentH - padY - subBarH - padY) / (cardH + cardGap)
        val startIdx = scrollOffset * cols
        val visible = filteredSkins.drop(startIdx).take(maxRows * cols)

        for ((idx, skinId) in visible.withIndex()) {
            val col = idx % cols
            val row = idx / cols
            val cx = px + padX + col * (cardW + cardGap)
            val cy = gridY + row * (cardH + cardGap)

            if (mx in cx..(cx + cardW) && my in cy..(cy + cardH)) {
                selectedSkin = if (selectedSkin == skinId) null else skinId
                return true
            }
        }

        return super.mouseClicked(mx.toDouble(), my.toDouble(), 0)
    }

    private fun handleGroupedSkinsClick(mx: Int, my: Int, contentY: Int, contentH: Int): Boolean {
        val padX = 10
        val padY = 6
        val subBarH = 20
        val headerH = 22
        val cardGap = 5
        val cols = 4
        val cardW = (panelW - padX * 2 - cardGap * (cols - 1)) / cols
        val cardH = 100
        val groupGap = 4

        val contentTop = contentY + padY + subBarH
        var curY = contentTop - groupScrollOffset

        for (group in skinSetGroups) {
            val isExpanded = group.setName in expandedSets

            // Group header click → toggle expand/collapse
            if (mx in (px + padX)..(px + panelW - padX) && my in curY..(curY + headerH)) {
                if (isExpanded) expandedSets.remove(group.setName)
                else expandedSets.add(group.setName)
                return true
            }
            curY += headerH + 2

            // Skin card clicks in expanded group
            if (isExpanded) {
                val rows = (group.skins.size + cols - 1) / cols
                for (row in 0 until rows) {
                    for (col in 0 until cols) {
                        val skinIdx = row * cols + col
                        if (skinIdx >= group.skins.size) break
                        val skinId = group.skins[skinIdx]
                        val cx = px + padX + col * (cardW + cardGap)
                        if (mx in cx..(cx + cardW) && my in curY..(curY + cardH)) {
                            selectedSkin = if (selectedSkin == skinId) null else skinId
                            return true
                        }
                    }
                    curY += cardH + cardGap
                }
            }
            curY += groupGap
        }

        return super.mouseClicked(mx.toDouble(), my.toDouble(), 0)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontal: Double, vertical: Double): Boolean {
        if (currentTab == Tab.ALL_SKINS) {
            if (groupBySet) {
                // Pixel-based scrolling for grouped view
                val scrollAmount = (vertical * 20).toInt()
                val maxScroll = calcGroupedContentHeight()
                groupScrollOffset = (groupScrollOffset - scrollAmount).coerceIn(0, maxOf(0, maxScroll))
            } else {
                val cols = 4
                val totalRows = (filteredSkins.size + cols - 1) / cols
                val contentH = panelH - titleBarH - divH
                val cardH = 130
                val cardGap = 6
                val maxRows = (contentH - 6 - 20 - 6) / (cardH + cardGap)
                val maxScroll = maxOf(0, totalRows - maxRows)
                scrollOffset = (scrollOffset - vertical.toInt()).coerceIn(0, maxScroll)
            }
        } else if (currentTab == Tab.PARTY && selectedSlot >= 0) {
            // Scroll through skins with mouse wheel in center area
            val mx = mouseX.toInt()
            if (mx > px + partyLeftW && mx < px + panelW - infoRightW) {
                val party = CobblemonClient.INSTANCE.getStorage().party
                val pokemon = party.get(selectedSlot)
                if (pokemon != null) {
                    val speciesId = pokemon.species.resourceIdentifier.toString()
                    val count = availableSkins.count { CobblemonSkinMod.skinSpeciesMap[it] == speciesId }
                    if (count > 0) {
                        partySkinIndex = (partySkinIndex - vertical.toInt()).coerceIn(0, count - 1)
                    }
                }
            }
        }
        return true
    }

    override fun charTyped(char: Char, modifiers: Int): Boolean {
        if (searchActive && currentTab == Tab.ALL_SKINS) {
            if (char.code >= 32) {
                searchQuery += char
                updateFilteredSkins()
                return true
            }
        }
        return super.charTyped(char, modifiers)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (searchActive && currentTab == Tab.ALL_SKINS) {
            when (keyCode) {
                259 -> { // BACKSPACE
                    if (searchQuery.isNotEmpty()) {
                        searchQuery = searchQuery.dropLast(1)
                        updateFilteredSkins()
                    }
                    return true
                }
                256 -> { // ESCAPE
                    searchActive = false
                    searchQuery = ""
                    updateFilteredSkins()
                    return true
                }
                257 -> { // ENTER
                    searchActive = false
                    return true
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun isPauseScreen() = false

    override fun removed() {
        super.removed()
        // Clean up all cached state to prevent memory leaks
        cachedPartyRenderables.fill(null)
        cachedSkinRenderables.clear()
        skinPreviewStates.clear()
    }
}
