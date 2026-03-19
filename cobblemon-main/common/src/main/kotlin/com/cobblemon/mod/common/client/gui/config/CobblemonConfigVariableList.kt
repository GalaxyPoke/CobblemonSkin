/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.config

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.text.yellow
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.HEIGHT
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.LABEL_Y_OFFSET
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.PADDING
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.SLOT_HEIGHT
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.SLOT_WIDTH
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigVariableList.CobblemonConfigVariableListEntry
import com.cobblemon.mod.common.client.gui.config.variable.*
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.config.Category
import com.cobblemon.mod.common.config.CobblemonConfig
import com.cobblemon.mod.common.config.CobblemonConfigField
import com.cobblemon.mod.common.config.CobblemonConfigField.CobblemonConfigSide.SERVER
import com.cobblemon.mod.common.util.asTranslated
import net.minecraft.SharedConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import kotlin.math.min
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class CobblemonConfigVariableList(
    val parent: CobblemonConfigScreen,
) : ContainerObjectSelectionList<CobblemonConfigVariableListEntry>(
    Minecraft.getInstance(),
    Minecraft.getInstance().window.guiScaledWidth,
    HEIGHT,
    0,
    SLOT_HEIGHT
) {

    val entries = mutableListOf<CobblemonConfigCategory>()

    init {
        this.x = 0
        this.y = minecraft.window.guiScaledHeight / 2 - HEIGHT / 2

        setRenderHeader(false, 0)
        addEntries()
    }

    @Suppress("UNCHECKED_CAST")
    private fun addEntries() {
        parent.clonedConfig::class.memberProperties.map { property ->
            if (!property.javaField!!.isAnnotationPresent(CobblemonConfigField::class.java)) {
                return@map null
            }

            val fieldAnnotation = property.javaField!!.getAnnotation(CobblemonConfigField::class.java)
            if (parent.hideServerConfigs && fieldAnnotation.side == SERVER) {
                return@map null
            }

            val entry: CobblemonConfigVariable? =
                when (val classifier = property.returnType.classifier) {
                    String::class -> CobblemonConfigStringVariable(this, property as KMutableProperty1<out CobblemonConfig, String>)
                    Int::class -> CobblemonConfigIntVariable(this, property as KMutableProperty1<out CobblemonConfig, Int>)
                    Float::class -> CobblemonConfigFloatVariable(this, property as KMutableProperty1<out CobblemonConfig, Float>)
                    Double::class -> CobblemonConfigDoubleVariable(this, property as KMutableProperty1<out CobblemonConfig, Double>)
                    Boolean::class -> CobblemonConfigBooleanVariable(this, property as KMutableProperty1<out CobblemonConfig, Boolean>)
                    is KClass<*> -> if (classifier.java.isEnum) {
                        CobblemonConfigEnumVariable(this, property as KMutableProperty1<out CobblemonConfig, Enum<*>>)
                    } else null
                    else -> null
                }

            if (entry == null) {
                if (SharedConstants.IS_RUNNING_IN_IDE) {
                    Cobblemon.LOGGER.warn("Widget for variable type: ${property.returnType.classifier} not registered: ${property.name}")
                }

                return@map null
            }

            return@map entry
        }
        .filterNotNull()
        .groupBy { it.cobblemonConfigFieldAnnotation.category }
        .toSortedMap(compareBy { it.ordinal })
        .forEach { category, variables ->
            val categoryEntry = CobblemonConfigCategory(this, category, variables)
            entries.add(categoryEntry)
        }

        entries.forEach { entry -> entry.add() }
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick)

        val hoveredVariableTooltip = this.hovered?.tooltip
        if (isMouseInsideScrollList(mouseX, mouseY) && hoveredVariableTooltip != null) {
            guiGraphics.renderTooltip(minecraft.font, hoveredVariableTooltip.toCharSequence(minecraft), mouseX, mouseY)
        }
    }

    fun isMouseInsideScrollList(mouseX: Int, mouseY: Int): Boolean {
        val bottomX = parent.width / 2 - SLOT_WIDTH / 2
        val upperX = parent.width / 2 + SLOT_WIDTH / 2

        return mouseX >= bottomX && mouseX <= upperX && mouseY >= y && mouseY <= bottom
    }

    override fun getRowWidth(): Int = width
    override fun getScrollbarPosition(): Int = width / 2 - SLOT_WIDTH / 2 + SLOT_WIDTH + PADDING

    fun filterConfigurations(text: String) {
        for (entry in entries) {
            entry.remove()
        }

        for (entry in entries) {
            if (text.isBlank() || entry.filter(text)) {
                entry.add(text)
            }
        }

        scrollAmount = min(maxScroll.toDouble(), scrollAmount)
    }

    abstract class CobblemonConfigVariableListEntry(
        val parent: CobblemonConfigVariableList,
    ) : Entry<CobblemonConfigVariableListEntry>() {
        val children = mutableListOf<GuiEventListener>()
        var tooltip: Tooltip? = null

        fun getX(left: Int) = parent.width / 2 - SLOT_WIDTH / 2
        fun getY(top: Int) = top

        abstract fun filter(text: String): Boolean

        override fun children(): MutableList<out GuiEventListener> = children
        override fun narratables(): MutableList<out NarratableEntry> = children.filterIsInstance<NarratableEntry>().toMutableList()
    }

    class CobblemonConfigCategory(
        parent: CobblemonConfigVariableList,
        val category: Category,
        val configVariables: List<CobblemonConfigVariable>
    ) : CobblemonConfigVariableListEntry(parent) {
        override fun render(
            context: GuiGraphics,
            index: Int,
            top: Int,
            left: Int,
            width: Int,
            height: Int,
            mouseX: Int,
            mouseY: Int,
            hovering: Boolean,
            partialTick: Float
        ) {
            drawScaledText(
                context = context,
                text = "cobblemon.config.ui.category.${category.lang}".asTranslated().yellow(),
                x = parent.width / 2,
                y = getY(top) + LABEL_Y_OFFSET,
                shadow = true,
                centered = true,
            )
        }

        fun add(filter: String? = null) {
            parent.addEntry(this)
            configVariables.forEach { variable ->
                if (filter == null || variable.filter(filter)) {
                    parent.addEntry(variable)
                }
            }
        }

        fun remove() {
            parent.removeEntry(this)
            configVariables.forEach { variable -> parent.removeEntry(variable) }
        }

        override fun filter(text: String): Boolean = configVariables.any { it.filter(text) }
    }
}