/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.config.variable

import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.PADDING
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.WIDGET_HEIGHT
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.WIDGET_WIDTH
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.WIDGET_Y_OFFSET
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigVariableList

import com.cobblemon.mod.common.config.CobblemonConfig
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

abstract class CobblemonConfigButtonVariable(
    parent: CobblemonConfigVariableList,
    config: KMutableProperty1<out CobblemonConfig, *>,
) : CobblemonConfigVariable(parent, config) {
    val button: Button =
        Button.builder(labelLang) { onButtonClick() }
            .size(WIDGET_WIDTH, WIDGET_HEIGHT)
            .build()

    init {
        children.addFirst(button)
    }

    abstract fun onButtonClick()

    override fun updateConfigValueDisplay() {
        button.message = Component.literal(getConfigValueAsString())
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
        super.render(context, index, top, left, width, height, mouseX, mouseY, hovering, partialTick)

        button.x = resetButton.x - button.width - PADDING
        button.y = getY(top) + WIDGET_Y_OFFSET
        button.render(context, mouseX, mouseY, partialTick)
    }
}

class CobblemonConfigBooleanVariable(
    parent: CobblemonConfigVariableList,
    config: KMutableProperty1<out CobblemonConfig, Boolean>,
) : CobblemonConfigButtonVariable(parent, config) {

    init {
        updateConfigValueDisplay()
    }

    override fun getConfigValueAsString(): String = (if (isSelected()) CommonComponents.GUI_YES else CommonComponents.GUI_NO).string
    override fun onButtonClick() = setConfigValue(!isSelected())

    private fun isSelected() = (config.getter.call(parent.parent.clonedConfig) as Boolean?) ?: false
}

class CobblemonConfigEnumVariable(
    parent: CobblemonConfigVariableList,
    config: KMutableProperty1<out CobblemonConfig, Enum<*>>,
) : CobblemonConfigButtonVariable(parent, config) {

    private var enumIndex = 0

    @Suppress("UNCHECKED_CAST")
    private val enumValues: Array<Enum<*>> = config.returnType.classifier
        .let { it as? KClass<Enum<*>> }
        ?.java?.enumConstants ?: emptyArray()

    init {
        val currentEnumValue = getCurrentEnumValue()
        enumValues.firstOrNull { it.name == currentEnumValue?.name }?.let {
            enumIndex = it.ordinal
        }
        updateConfigValueDisplay()
    }

    override fun getConfigValueAsString(): String {
        return getCurrentEnumValue()?.name?.let {
            lang("$labelEntry.${it.lowercase()}").string
        } ?: ""
    }

    private fun getConfigValueTooltipString(): String {
        return getCurrentEnumValue()?.name?.let {
            lang("$labelEntry.${it.lowercase()}.tooltip").string
        } ?: ""
    }

    override fun onButtonClick() {
        enumIndex = (enumIndex + 1) % enumValues.size
        val newEnumValue = enumValues[enumIndex]

        setConfigValue(newEnumValue)
    }

    override fun updateConfigValueDisplay() {
        super.updateConfigValueDisplay()
        tooltip = Tooltip.create(Component.literal(getConfigValueTooltipString()))
    }

    private fun getCurrentEnumValue(): Enum<*>? = (config.getter.call(parent.parent.clonedConfig) as Enum<*>?)
}