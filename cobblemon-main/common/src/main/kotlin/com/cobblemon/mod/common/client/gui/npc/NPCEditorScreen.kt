/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.npc

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.client.gui.behaviour.BehaviourEditorScreen
import com.cobblemon.mod.common.client.gui.npc.widgets.ConfigVariableList
import com.cobblemon.mod.common.client.gui.npc.widgets.NPCRenderWidget
import com.cobblemon.mod.common.client.gui.npc.widgets.SimpleNPCTextInputWidget
import com.cobblemon.mod.common.client.render.drawScaledTextJustifiedRight
import com.cobblemon.mod.common.net.messages.client.npc.dto.NPCConfigurationDTO
import com.cobblemon.mod.common.net.messages.server.npc.SaveNPCPacket
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.entity.LivingEntity

class NPCEditorScreen(
    val npcId: Int,
    val dto: NPCConfigurationDTO
) : Screen("gui.npc_editor.title".asTranslated()), CobblemonRenderable {
    companion object {
        const val BASE_WIDTH = 360
        const val BASE_HEIGHT = 220

        val baseResource = cobblemonResource("textures/gui/npc/base.png")
    }

    val middleX: Int
        get() = this.minecraft!!.window.guiScaledWidth / 2
    val middleY: Int
        get() = this.minecraft!!.window.guiScaledHeight / 2

    val leftX: Int
        get() = middleX - BASE_WIDTH / 2
    val topY: Int
        get() = middleY - BASE_HEIGHT / 2

    override fun init() {
        super.init()
        addRenderableOnly(NPCRenderWidget(leftX + 12, topY + 35, dto.npcUUID, dto.resourceIdentifier, dto.aspects))
        addRenderableWidget(SimpleNPCTextInputWidget(
            getter = { dto.npcName.string },
            setter = { dto.npcName = it.text() },
            posX = leftX + 12,
            posY = topY + 9,
            width = 192,
            height = 22,
            maxLength = 32
        ))

        addRenderableWidget(SimpleNPCTextInputWidget(
            getter = { dto.aspects.joinToString() },
            setter = {
                dto.aspects.clear()
                dto.aspects.addAll(it.split(",").map { it.trim() })
            },
            posX = leftX + 12,
            posY = topY + 155,
            width = 116,
            height = 40,
            maxLength = 72,
            wrap = true
        ))

        addRenderableWidget(
            NPCEditorButton(
                buttonX = leftX + 348F,
                buttonY = topY + 201F,
                label = lang("ui.generic.save"),
                alignRight = true
            ) {
                SaveNPCPacket(npcId, dto).sendToServer()
                this.minecraft!!.setScreen(null)
            }
        )

        addRenderableWidget(
            NPCEditorButton(
                buttonX = leftX + 12F,
                buttonY = topY + 201F,
                label = lang("ui.entity.behaviour_editor"),
            ) {
                val entity = this.minecraft!!.level!!.getEntity(npcId) as? LivingEntity ?: return@NPCEditorButton // Unlikely
                minecraft!!.setScreen(BehaviourEditorScreen(entity = entity, appliedBehaviours = dto.behaviours))
            }
        )

        addRenderableWidget(ConfigVariableList(leftX + 134, topY + 35, this))
    }

    override fun renderBlurredBackground(delta: Float) {}

    override fun renderMenuBackground(context: GuiGraphics) {}

    override fun isPauseScreen() = false

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        blitk(
            matrixStack = context.pose(),
            texture = baseResource,
            x = leftX,
            y = topY,
            height = BASE_HEIGHT,
            width = BASE_WIDTH
        )

        var extendedId = npcId.toString()
        while (extendedId.length < 12) extendedId = "0$extendedId"

        drawScaledTextJustifiedRight(
            context = context,
            text = extendedId.text(),
            x = leftX + 348,
            y = topY + 9,
            shadow = true
        )

        super.render(context, mouseX, mouseY, delta)
    }
}