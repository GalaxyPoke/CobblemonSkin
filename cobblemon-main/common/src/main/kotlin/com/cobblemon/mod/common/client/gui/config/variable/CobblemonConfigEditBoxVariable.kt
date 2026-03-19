/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.config.variable

import com.cobblemon.mod.common.api.text.add
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.PADDING
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.SLOT_HEIGHT
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.WIDGET_HEIGHT
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.WIDGET_WIDTH
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen.Companion.WIDGET_Y_OFFSET
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigVariableList
import com.cobblemon.mod.common.config.CobblemonConfig
import com.cobblemon.mod.common.config.constraint.IntConstraint
import com.cobblemon.mod.common.util.lang
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.EditBox.DEFAULT_TEXT_COLOR
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.jvm.javaField

abstract class CobblemonConfigEditBoxVariable(
    parent: CobblemonConfigVariableList,
    config: KMutableProperty1<out CobblemonConfig, *>,
) : CobblemonConfigVariable(parent, config) {
    val editBox = EditBox(
        Minecraft.getInstance().font,
        parent.x,
        parent.y,
        WIDGET_WIDTH,
        SLOT_HEIGHT,
        labelLang,
    ).also {
        it.height = WIDGET_HEIGHT
        it.setMaxLength(250)

        it.value = getConfigValueAsString()
    }

    init {
        children.addFirst(editBox)
    }

    override fun updateConfigValueDisplay() {
        editBox.value = getConfigValueAsString()
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

        editBox.x = resetButton.x - editBox.width - PADDING
        editBox.y = getY(top) + WIDGET_Y_OFFSET
        editBox.render(context, mouseX, mouseY, partialTick)
    }
}

class CobblemonConfigStringVariable(
    parent: CobblemonConfigVariableList,
    config: KMutableProperty1<out CobblemonConfig, String>,
) : CobblemonConfigEditBoxVariable(parent, config) {
    override fun getConfigValueAsString(): String = (config.getter.call(parent.parent.clonedConfig) as String?) ?: ""

    init {
        editBox.setResponder { value ->
            setConfigValue(value, false)
        }
    }
}

class CobblemonConfigIntVariable(
    parent: CobblemonConfigVariableList,
    config: KMutableProperty1<out CobblemonConfig, Int>,
) : CobblemonConfigEditBoxVariable(parent, config) {
    private val intConstraintAnnotation: IntConstraint? =
        config.javaField!!.annotations.firstOrNull{ it is IntConstraint } as IntConstraint?

    override fun getConfigValueAsString(): String = (config.getter.call(parent.parent.clonedConfig) as Int?).toString()

    init {
        editBox.setFilter { value -> value.toIntOrNull() != null || value.isBlank() || value == "-" }
        editBox.setResponder { value ->
            if (value.isBlank()) {
                return@setResponder
            }

            val intValue = value.toIntOrNull() ?: return@setResponder
            setConfigValue(intValue, false)

            if (intConstraintAnnotation != null) {
                val isBetweenIntConstraint = intValue >= intConstraintAnnotation.min && intValue <= intConstraintAnnotation.max
                if (isBetweenIntConstraint) {
                    tooltip = originalTooltip
                    editBox.setTextColor(DEFAULT_TEXT_COLOR)
                } else {
                    val errorLang = lang("config.ui.error.int_constraint", intConstraintAnnotation.min, intConstraintAnnotation.max).red()
                    tooltip = Tooltip.create(tooltipLang.plainCopy().add(Component.literal("\n")).add(errorLang))
                    ChatFormatting.RED.color?.let { editBox.setTextColor(it) }
                }
            }
        }
    }
}

class CobblemonConfigFloatVariable(
    parent: CobblemonConfigVariableList,
    config: KMutableProperty1<out CobblemonConfig, Float>,
) : CobblemonConfigEditBoxVariable(parent, config) {
    override fun getConfigValueAsString(): String = (config.getter.call(parent.parent.clonedConfig) as Float?).toString()

    init {
        editBox.setFilter { value -> value.toFloatOrNull() != null || value.isBlank() || value == "." || value == "-" }
        editBox.setResponder { value ->
            if (value.toFloatOrNull() != null) {
                setConfigValue(value.toFloat(), false)
            }
        }
    }
}

class CobblemonConfigDoubleVariable(
    parent: CobblemonConfigVariableList,
    config: KMutableProperty1<out CobblemonConfig, Double>,
) : CobblemonConfigEditBoxVariable(parent, config) {
    override fun getConfigValueAsString(): String = (config.getter.call(parent.parent.clonedConfig) as Double?).toString()

    init {
        editBox.setFilter { value -> value.toDoubleOrNull() != null || value.isBlank() || value == "." || value == "-" }
        editBox.setResponder { value ->
            if (value.toDoubleOrNull() != null) {
                setConfigValue(value.toDouble(), false)
            }
        }
    }
}