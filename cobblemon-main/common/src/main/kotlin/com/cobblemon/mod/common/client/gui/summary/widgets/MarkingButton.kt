/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.resources.ResourceLocation

class MarkingButton(
    val buttonX: Number,
    val buttonY: Number,
    var buttonState: Int,
    var canEdit: Boolean = true,
    private val resource: ResourceLocation,
    val clickAction: OnPress
): Button(buttonX.toInt(), buttonY.toInt(), (SIZE / 2), (SIZE / 2), "".text(), clickAction, DEFAULT_NARRATION), CobblemonRenderable {

    companion object {
        const val SIZE = 12
        const val SCALE = 0.5F
    }

    fun renderButton(context: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val matrices = context.pose()

        blitk(
            matrixStack = matrices,
            texture = resource,
            x = buttonX.toFloat() / SCALE,
            y = buttonY.toFloat() / SCALE,
            width = SIZE,
            height = SIZE,
            vOffset = buttonState * SIZE,
            uOffset = if (canEdit && isMouseOver(mouseX.toDouble(), mouseY.toDouble())) SIZE else 0,
            textureHeight = SIZE * 3,
            textureWidth = SIZE * 2,
            scale = SCALE
        )
    }

    override fun playDownSound(soundManager: SoundManager) {
        if (canEdit) soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        if (canEdit) super.onClick(mouseX, mouseY)
    }
}
