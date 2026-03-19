/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation

class SummaryTab(
    pX: Int, pY: Int,
    val label: MutableComponent? = null,
    val icon: ResourceLocation? = null,
    onPress: OnPress
): Button(pX, pY, WIDTH, HEIGHT, label ?: Component.empty(), onPress, DEFAULT_NARRATION), CobblemonRenderable {
    companion object {
        private const val WIDTH = 39
        private const val HEIGHT = 13
        private const val SCALE = 0.5F

        private val tabResource = cobblemonResource("textures/gui/summary/summary_tab.png")
    }

    private var isActive = false

    override fun renderWidget(context: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        val matrices = context.pose()
        if (isActive) {
            blitk(
                matrixStack = matrices,
                texture = tabResource,
                x = x,
                y = y,
                width = width,
                height = height
            )
        }

        if (icon !== null) {
            blitk(
                matrixStack = matrices,
                texture = icon,
                x = (x + 15.5) / SCALE,
                y = (y + 3.5) / SCALE,
                width = 17,
                height = 17,
                scale = SCALE
            )
        }

        if (label !== null && isMouseOver(pMouseX.toDouble(), pMouseY.toDouble())) {
            context.renderTooltip(Minecraft.getInstance().font, label, pMouseX, pMouseY)
        }
    }

    override fun playDownSound(soundManager: SoundManager) {
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
    }

    fun toggleTab(state: Boolean = true) {
        isActive = state
    }
}
