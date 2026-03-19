/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.common

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.mark.Mark
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

class MarkIcon(
    val x: Number,
    val y: Number,
    val mark: Mark,
    val showTooltip: Boolean = true,
    val opacity: Float = 1F
) {
    companion object {
        const val SIZE = 32
        const val SCALE = 0.5F
    }

    fun render(context: GuiGraphics, mouseX: Int, mouseY: Int) {
        blitk(
            matrixStack = context.pose(),
            texture = mark.texture,
            x = x.toFloat() / SCALE,
            y = y.toFloat() / SCALE,
            height = SIZE,
            width = SIZE,
            alpha = opacity,
            scale = SCALE
        )

        if (showTooltip && isHovered(mouseX, mouseY)) {
            context.renderTooltip(Minecraft.getInstance().font, mark.getName(), mouseX, mouseY)
        }
    }

    fun isHovered(mouseX: Int, mouseY: Int): Boolean =
        mouseX.toFloat() in (x.toFloat()..(x.toFloat() + (SIZE * SCALE)))
            && mouseY.toFloat() in (y.toFloat()..(y.toFloat() + (SIZE * SCALE)))
}
