/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class IconButton(
    pX: Int, pY: Int,
    val buttonWidth: Int,
    val buttonHeight: Int,
    val resource: ResourceLocation,
    val altResource: ResourceLocation? = null,
    val tooltipKey: String? = null,
    label: String,
    onPress: OnPress,
): Button(pX, pY, (buttonWidth * SCALE).toInt(), (buttonHeight * SCALE).toInt(), Component.literal(label), onPress, DEFAULT_NARRATION), CobblemonRenderable {

    companion object {
        private const val SCALE = 0.5F
    }

    var highlighted = false
    var showAlt = false

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {

        blitk(
            matrixStack = context.pose(),
            x = x / SCALE,
            y = y / SCALE,
            texture = if (altResource != null && showAlt) altResource else resource,
            width = buttonWidth,
            height = buttonHeight,
            vOffset = if (isHovered() || highlighted) buttonHeight else 0,
            textureHeight = buttonHeight * 2,
            scale = SCALE
        )

        if (isHovered()) {
            tooltipKey?.let {
                context.renderTooltip(Minecraft.getInstance().font, lang(it), mouseX, mouseY)
            }
        }
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0F))
    }
}
