/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.behaviour

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.npc.NPCEditorButton
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.behaviour.SetEntityBehaviourPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity

class BehaviourEditorScreen(
    val entity: LivingEntity,
    val appliedBehaviours: MutableSet<ResourceLocation>,
) : Screen(Component.literal("Behaviour Editor")) {
    companion object {
        const val BASE_WIDTH = 360
        const val BASE_HEIGHT = 220

        val baseResource = cobblemonResource("textures/gui/npc/base_behaviour.png")
    }
    var x = 0
    var y = 0

    lateinit var unadded: BehaviourOptionsList
    lateinit var added: BehaviourOptionsList

    override fun init() {
        super.init()
        minecraft = Minecraft.getInstance()
        x = (minecraft!!.window.guiScaledWidth / 2) - BASE_WIDTH / 2
        y = (minecraft!!.window.guiScaledHeight / 2) - BASE_HEIGHT / 2

        unadded = addRenderableWidget(
            BehaviourOptionsList(
                parent = this,
                left = x + 12,
                top = y + 52,
                entity = entity,
                appliedPresets = appliedBehaviours,
                addingMenu = true
            )
        )

        added = addRenderableWidget(
            BehaviourOptionsList(
                parent = this,
                left = x + 183,
                top = y + 52,
                entity = entity,
                appliedPresets = appliedBehaviours,
                addingMenu = false
            )
        )

        addRenderableWidget(
            NPCEditorButton(
                buttonX = x + 348F,
                buttonY = y + 201F,
                label = lang("ui.generic.save"),
                alignRight = true
            ) {
                // Send packet for updating behaviours of the entity
                // expect that it will reopen whichever GUI makes sense afterwards
                SetEntityBehaviourPacket(entity.id, appliedBehaviours.toSet()).sendToServer()
            }
        )
    }

    override fun renderBlurredBackground(delta: Float) {}

    override fun renderMenuBackground(context: GuiGraphics) {}

    override fun isPauseScreen() = false

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        blitk(
            matrixStack = context.pose(),
            texture = baseResource,
            x = x,
            y = y,
            height = BASE_HEIGHT,
            width = BASE_WIDTH
        )

        val name = if (entity is PokemonEntity) entity.pokemon.getDisplayName() else (entity.customName ?: entity.name)

        drawScaledText(
            context = context,
            text = lang("ui.entity.behaviour_editor.label", name.copy()),
            x = x + 12,
            y = y + 9,
            shadow = true
        )

        drawScaledText(
            context = context,
            text = lang("ui.entity.behaviour_editor.label.inactive"),
            x = x + 94,
            y = y + 38,
            centered = true,
            shadow = true
        )

        drawScaledText(
            context = context,
            text = lang("ui.entity.behaviour_editor.label.active"),
            x = x + 265,
            y = y + 38,
            centered = true,
            shadow = true
        )

        super.render(context, mouseX, mouseY, delta)
    }

    fun add(resourceLocation: ResourceLocation, alignButtonRight: Boolean) {
        appliedBehaviours.add(resourceLocation)
        unadded.removeEntry(resourceLocation)
        added.addEntry(resourceLocation, !alignButtonRight)
    }

    fun remove(resourceLocation: ResourceLocation, alignButtonRight: Boolean) {
        appliedBehaviours.remove(resourceLocation)
        added.removeEntry(resourceLocation)
        unadded.addEntry(resourceLocation, !alignButtonRight)
    }
}
