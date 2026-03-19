/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderType
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

abstract class TextWidget(
    pX: Int,
    pY: Int,
    width: Int = 91,
    height: Int = 14,
    maxLength: Int = 19,
    text: Component = "TextWidget".text(),
    update: () -> (Unit)
): EditBox(Minecraft.getInstance().font, pX, pY, width, height, text), CobblemonRenderable {

    var focusedTime: Long = 0
    var showCursor: Boolean = false
    var startPosX: Int = 0

    init {
        setMaxLength(maxLength)
        setResponder { update() }
        focusedTime = Util.getMillis()
        startPosX = x
    }

    fun applyTextCursor(string: String, placeholder: MutableComponent): MutableComponent {
        showCursor = isFocused && ((Util.getMillis() - this.focusedTime) / 300L % 2L == 0L)
        return if (isFocused) "${string}${if ((cursorPosition == string.length) && showCursor) "_" else ""}".text()
        else (if(string.isEmpty()) placeholder else string.text())
    }

    fun renderCursor(context: GuiGraphics, text: MutableComponent) {
        if (showCursor && !value.isEmpty() && cursorPosition != value.length) {
            val startToCursorWidth = Minecraft.getInstance().font.width((text.getString(cursorPosition).text().bold()).font(CobblemonResources.DEFAULT_LARGE))
            context.fill(
                RenderType.guiTextHighlight(),
                startPosX + startToCursorWidth - 1,
                y + 2,
                startPosX + startToCursorWidth,
                y + 11,
                -3092272
            )
        }
    }

    open fun unfocused() {
        isFocused = false
    }

    override fun setFocused(focused: Boolean) {
        if (focused) focusedTime = Util.getMillis()
        super.setFocused(focused)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isFocused && !isMouseOver(mouseX, mouseY)) unfocused()

        val result = super.mouseClicked(mouseX, mouseY, button)

        if (mouseX > startPosX + Minecraft.getInstance().font.width(value.text().bold().font(CobblemonResources.DEFAULT_LARGE))) {
            moveCursorToEnd(false)
        } else {
            var lineSubstring = ""
            for (char in value) {
                lineSubstring += char
                val startToLineWidth = Minecraft.getInstance().font.width(lineSubstring.text().bold().font(CobblemonResources.DEFAULT_LARGE))
                if ((mouseX - startPosX) <= startToLineWidth) break
            }
            moveCursorTo(maxOf(0, lineSubstring.length - 1), Screen.hasShiftDown())
        }
        return result
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (isFocused && (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER)) unfocused()
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
}
