/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pasture

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component

class PastureSlotIconConflictButton(
    var xPos: Int, var yPos: Int,
    onPress: OnPress
) : Button(xPos, yPos, WIDTH, HEIGHT, Component.literal("Pasture Defend"), onPress, DEFAULT_NARRATION), CobblemonRenderable {

    companion object {
        const val WIDTH = 15
        const val HEIGHT = 11
        const val ICON_SIZE = 11
        private const val SCALE = 0.5F

        private val baseResource = cobblemonResource("textures/gui/pasture/pasture_slot_button.png")
        private val baseActiveResource = cobblemonResource("textures/gui/pasture/pasture_slot_button_active.png")
        private val iconResource = cobblemonResource("textures/gui/pasture/pasture_slot_icon_defend.png")
    }
    private var enabled: Boolean = false

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        blitk(
            matrixStack = context.pose(),
            x = xPos,
            y = yPos,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (isHovered(mouseX.toDouble(), mouseY.toDouble())) HEIGHT else 0,
            textureHeight = HEIGHT * 2,
            texture = if (enabled) baseActiveResource else baseResource,
        )

        blitk(
            matrixStack = context.pose(),
            x = (xPos + 5) / SCALE,
            y = (yPos + 3) / SCALE,
            width = ICON_SIZE,
            height = ICON_SIZE,
            vOffset = if (enabled && isHovered(mouseX.toDouble(), mouseY.toDouble())) HEIGHT else 0,
            textureHeight = ICON_SIZE * 2,
            texture = iconResource,
            scale = SCALE
        )
    }

    fun setPos(x: Int, y: Int) {
        xPos = x
        yPos = y
    }

    override fun playDownSound(pHandler: SoundManager) {}

    fun isHovered(mouseX: Double, mouseY: Double) = mouseX.toFloat() in (xPos.toFloat()..(xPos.toFloat() + WIDTH)) && mouseY.toFloat() in (yPos.toFloat()..(yPos.toFloat() + HEIGHT))
}
