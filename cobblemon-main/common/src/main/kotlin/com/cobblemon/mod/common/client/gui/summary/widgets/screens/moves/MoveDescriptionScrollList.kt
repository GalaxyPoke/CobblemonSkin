/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves

import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

class MoveDescriptionScrollList(
    private val listX: Int,
    private val listY: Int,
    slotHeight: Int
) : ObjectSelectionList<MoveDescriptionEntry>(
    Minecraft.getInstance(),
    60, // width
    30, // height
    0, // top
    slotHeight
), CobblemonRenderable {
    private var scrolling = false

    init {
        this.y = this.listY
        this.x = this.listX
        this.setRenderHeader(false, 0)
    }

    override fun getScrollbarPosition(): Int {
        return x + width - 3
    }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, f: Float) {
        isHovered = pMouseX >= x && pMouseY >= y && pMouseX < x + width && pMouseY < y + height
        context.enableScissor(
            x,
            y,
            x + width,
            y + height
        )
        super.renderWidget(context, pMouseX, pMouseY, f)
        context.disableScissor()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        updateScrollingState(mouseX, mouseY)
        if (scrolling) isDragging = true

        return isDragging
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (scrolling) {
            if (mouseY < y) {
                scrollAmount = 0.0
            } else if (mouseY > bottom) {
                scrollAmount = maxScroll.toDouble()
            } else {
                scrollAmount += deltaY
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun renderListBackground(guiGraphics: GuiGraphics) {}

    private fun updateScrollingState(mouseX: Double, mouseY: Double) {
        scrolling = mouseX >= getScrollbarPosition().toDouble()
                && mouseX < (getScrollbarPosition() + 3).toDouble()
                && mouseY >= y
                && mouseY < bottom
    }

    fun setMoveDescription(moveDescription: MutableComponent) {
        clearEntries()
        val splitWidth = 100
        val text = moveDescription.string
        val words = text.split(" ")
        val splitText = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (Minecraft.getInstance().font.width(currentLine.toString() + word) > splitWidth) {
                splitText.add(currentLine.toString())
                currentLine = StringBuilder(word)
            } else {
                if (currentLine.isNotEmpty()) {
                    currentLine.append(" ")
                }
                currentLine.append(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            splitText.add(currentLine.toString())
        }

        for (part in splitText) {
            addEntry(MoveDescriptionEntry(Component.literal(part)))
        }
    }
}
