/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.behaviour

import com.cobblemon.mod.common.CobblemonBehaviours
import com.cobblemon.mod.common.api.ai.CobblemonBehaviour
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.behaviour.BehaviourOptionsList.BehaviourOptionSlot
import com.cobblemon.mod.common.client.gui.npc.NPCEditorButton
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.WidgetTooltipHolder
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity

class BehaviourOptionsList(
    val parent: BehaviourEditorScreen,
    val left: Int,
    val top: Int,
    val entity: LivingEntity,
    val appliedPresets: MutableSet<ResourceLocation>,
    val addingMenu: Boolean // Whether this is the list for the un-added presets
) : ContainerObjectSelectionList<BehaviourOptionSlot>(
    Minecraft.getInstance(),
    165,
    143,
    top,
    21
) {

    init {
        x = left
        if (addingMenu) {
            val unaddedPresets = CobblemonBehaviours.behaviours.filter { it.value.visible && it.value.canBeApplied(entity) && it.key !in appliedPresets }
            unaddedPresets.forEach { (key, value) -> addEntry(BehaviourOptionSlot(this, key, value, addingMenu)) }
        } else {
            val addedPresets = appliedPresets.mapNotNull { it to (CobblemonBehaviours.behaviours[it]?.takeIf { it.visible && it.canBeApplied(entity) } ?: return@mapNotNull null) }
            addedPresets.forEach { (key, value) -> addEntry(BehaviourOptionSlot(this, key, value, addingMenu)) }
        }
    }

    override fun renderListBackground(context: GuiGraphics) {}

    override fun renderListSeparators(guiGraphics: GuiGraphics) {}

    override fun getScrollbarPosition() = left + width - 6

    fun removeEntry(entry: ResourceLocation) {
        children().find { it.resourceLocation == entry }?.let { removeEntry(it) }
        // Resize scrollbar to avoid visual issues upon removal
        setScrollAmount(if (children().size < 7) 0.0 else getScrollAmount())
    }

    fun addEntry(entry: ResourceLocation, alignButtonRight: Boolean) {
        val behaviour = CobblemonBehaviours.behaviours[entry] ?: return
        addEntry(BehaviourOptionSlot(this, entry, behaviour, alignButtonRight))
    }

    class BehaviourOptionSlot(val parent: BehaviourOptionsList, val resourceLocation: ResourceLocation, val behaviour: CobblemonBehaviour, val alignButtonRight: Boolean = true) : Entry<BehaviourOptionSlot>() {
        companion object {
            val SLOT_WIDTH = 140
            val BUTTON_WIDTH = 16
            val ICON_WIDTH = 15
        }

        val children = mutableListOf<GuiEventListener>()

        val tooltip = Tooltip.create(behaviour.description.copy())
        val tooltipHolder = WidgetTooltipHolder().also { it.set(tooltip) }

        val applyButton = NPCEditorButton(
            0F,
            0F,
            buttonWidth = BUTTON_WIDTH
        ) {
            if (parent.addingMenu) {
                parent.parent.add(resourceLocation, alignButtonRight)
            } else {
                parent.parent.remove(resourceLocation, alignButtonRight)
            }
        }

        val slotRow = NPCEditorButton(
            0F,
            0F,
            behaviour.name.copy(),
            buttonWidth = SLOT_WIDTH,
            buttonResource = cobblemonResource("textures/gui/npc/button_base_${ if (alignButtonRight) "disabled" else "inactive" }.png"),
            buttonBorderResource = cobblemonResource("textures/gui/npc/button_border_${ if (alignButtonRight) "disabled" else "inactive" }.png")
        ) {}

        override fun children() = listOf(applyButton)

        override fun narratables() = children.filterIsInstance<NarratableEntry>()

        override fun setFocused(focused: Boolean) {}
        override fun isFocused() = false

        override fun render(
            guiGraphics: GuiGraphics,
            index: Int,
            top: Int,
            left: Int,
            width: Int,
            height: Int,
            mouseX: Int,
            mouseY: Int,
            hovering: Boolean,
            partialTick: Float
        ) {
            slotRow.x = parent.left + 5 + (if (alignButtonRight) 0 else (BUTTON_WIDTH - 1))
            slotRow.y = top + 1
            slotRow.buttonWidth = SLOT_WIDTH - (if (parent.children().size > 6) 6 else 0)

            applyButton.x = parent.left + 5 + (if (alignButtonRight) SLOT_WIDTH - (if (parent.children().size > 6) 7 else (1)) else 0)
            applyButton.y = top + 1

            applyButton.render(guiGraphics, mouseX, mouseY, partialTick)

            blitk(
                matrixStack = guiGraphics.pose(),
                texture = cobblemonResource("textures/gui/npc/icon_move.png"),
                x = (applyButton.x + 4.5) / 0.5,
                y = (applyButton.y + 4.5) / 0.5,
                width = ICON_WIDTH,
                height = ICON_WIDTH,
                vOffset = if (alignButtonRight) 0 else ICON_WIDTH,
                textureHeight = ICON_WIDTH * 2,
                scale = 0.5F
            )

            slotRow.render(guiGraphics, mouseX, mouseY, partialTick)

            if (hovering) {
                tooltipHolder.refreshTooltipForNextRenderPass(hovering, false, ScreenRectangle(left, top, width, height))
            }
        }
    }
}

