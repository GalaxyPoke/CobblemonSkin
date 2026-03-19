/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.CommonComponents
import net.minecraft.resources.ResourceLocation

class VariableObjectSelectionScrollbar(var parent: VariableObjectSelectionList, x: Int, y: Int, width: Int, height: Int): AbstractWidget(x, y, width, height, CommonComponents.EMPTY) {
    companion object {
        private val scrollerResource: ResourceLocation = ResourceLocation("minecraft", "textures/gui/sprites/widget/scroller.png")
        private val scrollerBackgroundResource: ResourceLocation = ResourceLocation("minecraft", "textures/gui/sprites/widget/scroller_background.png")

        private const val SCROLLER_HEIGHT = 32
    }

    private var scrolling = false
    private var scrollHoldOffset = 0.0

    fun scrollbarVisible(): Boolean = parent.getMaxScroll() > 0

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (scrollbarVisible()) {
            val scrollPosition = (parent.scrollAmount / parent.getMaxScroll()) * (height - SCROLLER_HEIGHT)

            blitk(
                matrixStack = guiGraphics.pose(),
                texture = scrollerBackgroundResource,
                x = x,
                y = y,
                width = width,
                height = height,
            )

            blitk(
                matrixStack = guiGraphics.pose(),
                texture = scrollerResource,
                x = x,
                y = y + scrollPosition,
                width = width,
                height = SCROLLER_HEIGHT,
                textureWidth = 6,
                textureHeight = SCROLLER_HEIGHT
            )
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        return parent.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (mouseX.toInt() in x..(x + width) && mouseY.toInt() in y..(y + height)) {
            if (button == 0) {
                var scrollPosition = (parent.scrollAmount / parent.getMaxScroll()) * (height - SCROLLER_HEIGHT)
                if (mouseY !in (y + scrollPosition)..(y + scrollPosition + SCROLLER_HEIGHT)) {
                    val scrollRatio =
                        ((mouseY - y - SCROLLER_HEIGHT / 2) / (height - SCROLLER_HEIGHT)).coerceIn(0.0, 1.0)
                    parent.setScroll(scrollRatio * parent.getMaxScroll())
                    scrollPosition = (parent.scrollAmount / parent.getMaxScroll()) * (height - SCROLLER_HEIGHT)
                }
                scrollHoldOffset = mouseY - y - scrollPosition
                scrolling = true
                return true
            }
        }
        return false
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (button == 0 && scrolling) {
            val scrollRatio = ((mouseY - y - scrollHoldOffset) / (height - SCROLLER_HEIGHT)).coerceIn(0.0,1.0)
            parent.setScroll(scrollRatio * parent.getMaxScroll())
            return true
        }
        return false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && scrolling) {
            scrolling = false
            return true
        }
        return false
    }

    override fun playDownSound(handler: SoundManager) {}
    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}
}