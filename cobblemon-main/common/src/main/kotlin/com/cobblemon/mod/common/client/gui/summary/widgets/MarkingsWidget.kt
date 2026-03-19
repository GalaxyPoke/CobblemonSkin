/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets

import com.cobblemon.mod.common.CobblemonNetwork.sendToServer
import com.cobblemon.mod.common.net.messages.server.pokemon.update.SetMarkingsPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

class MarkingsWidget(
    val pX: Number,
    val pY: Number,
    var pokemon: Pokemon?,
    var canEdit: Boolean = true,
): SoundlessWidget(pX.toInt(), pY.toInt(), WIDTH, HEIGHT, Component.literal("MarkingsWidget")) {
    companion object {
        private const val WIDTH = 82
        private const val HEIGHT = 12
    }

    var markingStates: MutableList<Int> = mutableListOf(0, 0, 0, 0, 0, 0)
    var markings: MutableList<MarkingButton> = mutableListOf()

    init { setActivePokemon(pokemon) }

    fun incrementState(state: Int) : Int = (state + 1) % 3

    fun setActivePokemon(newPokemon: Pokemon?) {
        pokemon = newPokemon

        children.clear()
        pokemon?.let {
            markingStates = it.markings.toMutableList()
            markingStates.forEachIndexed { index, state ->
                addWidget(MarkingButton(
                    buttonX = pX.toFloat() + (index * ((MarkingButton.SIZE / 2) + 1)),
                    buttonY = pY,
                    buttonState = state,
                    canEdit = canEdit,
                    resource = cobblemonResource("textures/gui/summary/icon_marking_${index}.png")
                ) {
                    val nextState = incrementState(markingStates.get(index))
                    markingStates.set(index, nextState)
                    (it as MarkingButton).buttonState = nextState
                })
            }
        }
    }

    fun saveMarkingsToPokemon(isParty: Boolean = true) {
        pokemon?.let {
            val currentStates = markingStates.toList()
            if (it.markings != currentStates) sendToServer(SetMarkingsPacket(it.uuid, currentStates, isParty))
        }
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        children.forEach { (it as MarkingButton).renderButton(context, mouseX, mouseY, partialTicks) }
    }
}
