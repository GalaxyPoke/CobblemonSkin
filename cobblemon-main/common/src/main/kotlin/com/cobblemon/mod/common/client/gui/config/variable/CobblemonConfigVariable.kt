/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.config.variable

import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.LABEL_Y_OFFSET
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.RESET_SLOT_WIDTH
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.SLOT_WIDTH
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.WIDGET_HEIGHT
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.WIDGET_Y_OFFSET
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigVariableList
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigVariableList.CobblemonConfigVariableListEntry
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.config.CobblemonConfig
import com.cobblemon.mod.common.config.CobblemonConfigField
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.MutableComponent
import java.text.Normalizer
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.jvm.javaField

abstract class CobblemonConfigVariable(
    parent: CobblemonConfigVariableList,
    val config: KMutableProperty1<out CobblemonConfig, *>,
) : CobblemonConfigVariableListEntry(parent) {
    val cobblemonConfigFieldAnnotation: CobblemonConfigField =
        config.javaField!!.annotations.firstOrNull{ it is CobblemonConfigField } as CobblemonConfigField

    val labelEntry = "config.ui.${cobblemonConfigFieldAnnotation.lang}"
    val tooltipEntry = "$labelEntry.tooltip"
    val labelLang: MutableComponent = lang(labelEntry)
    val tooltipLang: MutableComponent = lang(tooltipEntry)
    val originalTooltip: Tooltip? = if (!tooltipLang.string.endsWith(tooltipEntry)) Tooltip.create(tooltipLang) else null

    val resetButton: Button =
        Button.builder(lang("config.ui.reset")) {
            val defaultValue = config.getter.call(parent.parent.defaultConfig)
            setConfigValue(defaultValue)
        }
        .size(RESET_SLOT_WIDTH, WIDGET_HEIGHT)
        .build()

    init {
        tooltip = originalTooltip
        children.add(resetButton)
        updateResetButton()
    }

    abstract fun getConfigValueAsString(): String
    abstract fun updateConfigValueDisplay()

    fun setConfigValue(value: Any?, updateConfigValueDisplay: Boolean = true) {
        config.setter.call(parent.parent.clonedConfig, value)

        if (updateConfigValueDisplay) {
            updateConfigValueDisplay()
        }

        updateResetButton()
    }

    private fun updateResetButton() {
        resetButton.active = config.getter.call(parent.parent.clonedConfig) != config.getter.call(parent.parent.defaultConfig)
    }

    override fun filter(text: String): Boolean {
        val normalizedLabel = Normalizer.normalize(labelLang.string, Normalizer.Form.NFD).replace("\\p{M}+".toRegex(), "")
        return normalizedLabel.contains(text, ignoreCase = true)
    }

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
        val x = getX(left)
        val y = getY(top)

        drawScaledText(
            context = context,
            text = labelLang,
            x = getX(left),
            y = getY(top) + LABEL_Y_OFFSET,
            shadow = true
        )

        resetButton.x = x + SLOT_WIDTH - RESET_SLOT_WIDTH
        resetButton.y = y + WIDGET_Y_OFFSET
        resetButton.render(context, mouseX, mouseY, partialTick)
    }
}