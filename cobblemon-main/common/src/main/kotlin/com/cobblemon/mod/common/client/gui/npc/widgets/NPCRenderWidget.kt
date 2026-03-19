/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.npc.widgets

import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.gui.drawProfile
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import java.util.UUID
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.resources.ResourceLocation

class NPCRenderWidget(
    val x: Int,
    val y: Int,
    val entityId: UUID,
    var identifier: ResourceLocation,
    val aspects: MutableSet<String>
) : CobblemonRenderable, GuiEventListener {
    val state = FloatingState().also {
        it.currentAspects = aspects
        it.runtime.environment.setSimpleVariable("texture", StringValue(entityId.toString()))
    }

    companion object {
        const val WIDTH = 116
        const val HEIGHT = 112
    }

    override fun isFocused() = false
    override fun setFocused(focused: Boolean) {}
    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {

        context.enableScissor(
            x,
            y,
            x + WIDTH,
            y + HEIGHT
        )
        context.pose().pushPose()
        // Decrease on Z-axis to prevent model from rendering above other components like tooltips
        context.pose().translate(x + (WIDTH / 2F), y + HEIGHT + (HEIGHT / 4F), -500F)

        drawProfile(
            resourceIdentifier = identifier,
            matrixStack = context.pose(),
            partialTicks = delta,
            scale = 120F,
            state = state
        )

        context.pose().popPose()
        context.disableScissor()
    }
}