/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.MutableComponent

private val tooltipEdge = cobblemonResource("textures/gui/pokedex/tooltip_edge.png")
private val tooltipBackground = cobblemonResource("textures/gui/pokedex/tooltip_background.png")

fun renderTooltip(context: GuiGraphics, text: MutableComponent, posX: Int, posY: Int, delta: Float, offsetY: Int = 0) {
    val poseStack = context.pose()
    val textWidth = Minecraft.getInstance().font.width(text.font(CobblemonResources.DEFAULT_LARGE))
    val tooltipWidth = textWidth + 6
    val tooltipHeight = 11
    val tooltipTop = posY + offsetY

    poseStack.pushPose()
    poseStack.translate(0.0, 0.0, 1000.0)
    Minecraft.getInstance().let {
        it.mainRenderTarget.bindWrite(false)
        context.enableScissor(
            posX - (tooltipWidth / 2),
            tooltipTop + 1,
            (posX - (tooltipWidth / 2)) + tooltipWidth,
            tooltipTop + 10
        )
        it.gameRenderer.processBlurEffect(delta)
        context.disableScissor()
        it.mainRenderTarget.bindWrite(true)
    }

    blitk(matrixStack = poseStack, texture = tooltipEdge, x = posX - (tooltipWidth / 2) - 1, y = tooltipTop, width = 1, height = tooltipHeight)
    blitk(matrixStack = poseStack, texture = tooltipBackground, x = posX - (tooltipWidth / 2), y = tooltipTop, width = tooltipWidth, height = tooltipHeight)
    blitk(matrixStack = poseStack, texture = tooltipEdge, x = posX + (tooltipWidth / 2), y = tooltipTop, width = 1, height = tooltipHeight)
    drawScaledText(context = context, font = CobblemonResources.DEFAULT_LARGE, text = text, x = posX, y = tooltipTop + 1, shadow = true, centered = true)
    poseStack.popPose()
}
