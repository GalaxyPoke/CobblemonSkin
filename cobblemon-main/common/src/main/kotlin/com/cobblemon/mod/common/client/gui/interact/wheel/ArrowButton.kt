/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.interact.wheel

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component

class ArrowButton(
    pX: Int, pY: Int,
    val isRight: Boolean,
    onPress: OnPress
) : Button(pX, pY, WIDTH, HEIGHT, Component.empty(), onPress, DEFAULT_NARRATION), CobblemonRenderable {

    companion object {
        private val ARROW_LEFT_RESOURCE = cobblemonResource("textures/gui/interact/interact_wheel_arrow_left.png")
        private val ARROW_RIGHT_RESOURCE = cobblemonResource("textures/gui/interact/interact_wheel_arrow_right.png")

        private const val WIDTH = 10
        private const val HEIGHT = 15
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        blitk(
            matrixStack = context.pose(),
            x = x,
            y = y,
            texture = if (isRight) ARROW_RIGHT_RESOURCE else ARROW_LEFT_RESOURCE,
            width = WIDTH,
            height = HEIGHT,
            vOffset = if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) HEIGHT else 0,
            textureHeight = HEIGHT * 2
        )
    }
}
