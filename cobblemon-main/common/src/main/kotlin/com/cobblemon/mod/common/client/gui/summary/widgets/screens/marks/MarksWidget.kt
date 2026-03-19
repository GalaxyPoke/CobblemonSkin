/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens.marks

import com.cobblemon.mod.common.CobblemonNetwork.sendToServer
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.ColourLibrary
import com.cobblemon.mod.common.api.gui.MultiLineLabelK
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.mark.Mark
import com.cobblemon.mod.common.client.gui.common.MarkIcon
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.net.messages.server.pokemon.update.SetActiveMarkPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component

class MarksWidget(
    pX: Int, pY: Int,
    val pokemon: Pokemon
): SoundlessWidget(pX, pY, WIDTH, HEIGHT, Component.literal("MarksWidget")) {
    companion object {
        private const val WIDTH = 134
        private const val HEIGHT = 148
        const val SCALE = 0.5F

        private val marksBaseResource = cobblemonResource("textures/gui/summary/summary_marks_base.png")
    }

    var activeMark: Mark? = null
    var selectedMark: Mark? = null
    var hoveredMark: Mark? = null

    val marksScrollList = MarksScrollingWidget(
        pX = pX + 9,
        pY = pY + 45,
        setSelectedMark = {
            if (hoveredMark != null) {
                selectedMark = hoveredMark
                Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
            }
        },
        setHoveredMark = { hoveredMark = it }
    )

    init {
        children.clear()
        activeMark = pokemon.activeMark
        selectedMark = pokemon.activeMark
        val marksList: MutableList<Mark?> = pokemon.marks.sortedBy { it.identifier.toString() }.toMutableList()
        while ((marksList.size < 30) || (marksList.size > 30 && marksList.size % 6 != 0)) marksList.add(null)
        marksScrollList.createEntries(marksList)
        addWidget(marksScrollList)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (selectedMark !== null && isSelectedMarkHovered(mouseX.toInt(), mouseY.toInt())) {
            selectedMark = null
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val matrices = context.pose()

        blitk(
            matrixStack = matrices,
            texture = marksBaseResource,
            x = x,
            y = y,
            width = width,
            height = height
        )

        if (selectedMark != null) {
            MarkIcon(
                x = x + 12,
                y = y + 12,
                selectedMark!!
            ).render(context, mouseX, mouseY)

            matrices.pushPose()
            matrices.scale(SCALE, SCALE, 1F)
            MultiLineLabelK.create(
                component = selectedMark!!.getDescription(),
                width = 85 / SCALE,
                maxLines = 4
            ).renderLeftAligned(
                context = context,
                x = (x + 38) / SCALE,
                y = (y + 11) / SCALE,
                ySpacing = 5 / SCALE,
                colour = ColourLibrary.WHITE,
                shadow = true
            )
            matrices.popPose()
        }

        // Title
        (if (selectedMark?.title != null) selectedMark!!.getTitle(pokemon.getDisplayName()) else pokemon.getDisplayName())?.let {
            drawScaledText(
                context = context,
                text = it,
                x = x + (width / 2),
                y = y + 38,
                scale = SCALE,
                centered = true
            )
        }

        marksScrollList.render(context, mouseX, mouseY, partialTicks)
        if (hoveredMark !== null && marksScrollList.isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            context.renderTooltip(Minecraft.getInstance().font, hoveredMark!!.getName(), mouseX, mouseY)
        }
    }

    fun isSelectedMarkHovered(mouseX: Int, mouseY: Int): Boolean {
        val markX = x + 12
        val markY = y + 12
        return mouseX in (markX..(markX + (MarkIcon.SIZE * MarkIcon.SCALE).toInt()))
                && mouseY in (markY..(markY + (MarkIcon.SIZE * MarkIcon.SCALE).toInt()))
    }

    fun saveActiveMarkToPokemon() {
        if (selectedMark != activeMark) sendToServer(SetActiveMarkPacket(pokemon.uuid, selectedMark))
    }
}
