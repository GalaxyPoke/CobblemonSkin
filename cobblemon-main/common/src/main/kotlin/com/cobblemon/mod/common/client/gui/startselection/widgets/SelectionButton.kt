/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.startselection.widgets

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.ColourLibrary
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component

class SelectionButton(
    pX: Int, pY: Int,
    pWidth: Int, pHeight: Int,
    onPress: OnPress
): Button(pX, pY, pWidth, pHeight, Component.literal("SelectionButton"), onPress, DEFAULT_NARRATION),
    CobblemonRenderable {

    companion object {
        private val buttonResource = cobblemonResource("textures/gui/starterselection/choose_button.png")
        const val BUTTON_WIDTH = 106
        const val BUTTON_HEIGHT = 14
        const val TEXT_HEIGHT = 10
    }

    override fun playDownSound(soundManager: SoundManager) {}

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val matrices = context.pose()
        blitk(
            matrixStack = matrices,
            texture = buttonResource,
            x = x,
            y = y,
            width = BUTTON_WIDTH,
            height = BUTTON_HEIGHT,
            textureHeight = BUTTON_HEIGHT * 2,
            vOffset = if (isHovered) BUTTON_HEIGHT else 0
        )

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.starter.choosebutton").bold(),
            x = x + BUTTON_WIDTH / 2,
            y = y + BUTTON_HEIGHT / 2 - TEXT_HEIGHT / 2,
            shadow = true,
            centered = true
        )
    }
}