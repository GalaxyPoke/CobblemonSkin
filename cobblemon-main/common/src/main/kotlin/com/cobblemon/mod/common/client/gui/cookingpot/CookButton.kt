/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.cookingpot

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.block.campfirepot.CampfirePotColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents

class CookButton(
    pX: Int, pY: Int,
    var selected: Boolean = false,
    var color: CampfirePotColor,
    onPress: OnPress
): Button(pX, pY, SIZE.toInt(), SIZE.toInt(), Component.literal("Cook"), onPress, DEFAULT_NARRATION) {

    companion object {
        private const val SIZE = 20F

        private val buttonResource = cobblemonResource("textures/gui/campfirepot/button.png")
    }

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val closedIcon = cobblemonResource("textures/item/campfire_pots/campfire_pot_${color.suffix}.png")
        val openIcon = cobblemonResource("textures/item/campfire_pots/campfire_pot_${color.suffix}_open.png")
        blitk(
            matrixStack = context.pose(),
            texture = buttonResource,
            x = x,
            y = y,
            width = SIZE,
            height = SIZE,
            vOffset = if (isMouseOver(pMouseX.toDouble(), pMouseY.toDouble())) SIZE else 0,
            textureHeight = SIZE * 2
        )

        blitk(
            matrixStack = context.pose(),
            texture = if (selected) closedIcon else openIcon,
            x = x + 2,
            y = y + 2,
            width = 16,
            height = 16
        )
    }

    override fun playDownSound(soundManager: SoundManager) {
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F))
    }
}