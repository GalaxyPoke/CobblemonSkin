/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.startselection.widgets

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.client.gui.PokemonGUIAnimationStyle
import com.cobblemon.mod.common.client.gui.VariableObjectSelectionList
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.gui.startselection.StarterSelectionScreen
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.config.starter.RenderableStarterCategory
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import org.joml.Quaternionf
import org.joml.Vector3f

class CategoryList(
    private val paneWidth: Int,
    private val paneHeight: Int,
    private val categories: List<RenderableStarterCategory>,
    val listX: Int,
    val listY: Int,
    private val minecraft: Minecraft = Minecraft.getInstance(),
    private val starterSelectionScreen: StarterSelectionScreen
) : VariableObjectSelectionList(
    listX,
    listY,
    paneWidth,
    paneHeight
), CobblemonRenderable {

    companion object {
        private val headerResource = cobblemonResource("textures/gui/starterselection/selection_container_header.png")
        private val footerResource = cobblemonResource("textures/gui/starterselection/selection_container_footer.png")
        private val edgeResource = cobblemonResource("textures/gui/starterselection/selection_container_edge.png")
        private val containerResource = cobblemonResource("textures/gui/starterselection/selection_container.png")

        private val buttonResource = cobblemonResource("textures/gui/starterselection/starter_selection.png")
        private val buttonActiveResource = cobblemonResource("textures/gui/starterselection/starter_selection_active.png")

        const val MARGIN = 4
        const val HEADER_HEIGHT = 7
        const val BUTTON_SEPERATION = 3
        const val CONTAINER_BUTTON_SIZE = 25
        const val CONTAINER_WIDTH = 89
    }

    init {
        addEntry(EmptySeparator(MARGIN))
        createEntries().forEach {
            addEntry(it)
            addEntry(EmptySeparator(MARGIN))
        }
    }

    private fun createEntries() = categories.map { category ->
        val rows = (category.pokemon.size - 1) / 3 + 1
        val height = CONTAINER_BUTTON_SIZE * rows + BUTTON_SEPERATION * (rows + 1) + HEADER_HEIGHT + 3
        Category(category, height)
    }

    inner class Category(private val category: RenderableStarterCategory, height: Int) : Entry(height) {

        override fun render(
            guiGraphics: GuiGraphics,
            index: Int,
            x: Int,
            y: Int,
            mouseX: Int,
            mouseY: Int,
            partialTick: Float
        ) {
            val rows = (category.pokemon.size - 1) / 3 + 1
            val matrices = guiGraphics.pose()
            //Header
            blitk(
                matrixStack = matrices,
                texture = headerResource,
                x = x + MARGIN,
                y = y,
                width = CONTAINER_WIDTH,
                height = HEADER_HEIGHT
            )

            val smallTextScale = 0.5F
            drawScaledText(
                context = guiGraphics,
                text = category.displayNameText.bold(),
                x = x + MARGIN + CONTAINER_WIDTH / 2,
                y = y + 2,
                shadow = true,
                centered = true,
                scale = smallTextScale
            )

            //Container
            var stackHeight = HEADER_HEIGHT
            blitk(
                matrixStack = matrices,
                texture = edgeResource,
                x = x + MARGIN,
                y = y + stackHeight,
                width = CONTAINER_WIDTH,
                height = 1
            )
            stackHeight += 1

            val containerInnerHeight = CONTAINER_BUTTON_SIZE * rows + BUTTON_SEPERATION * (rows + 1)

            blitk(
                matrixStack = matrices,
                texture = containerResource,
                x = x + MARGIN,
                y = y + stackHeight,
                width = CONTAINER_WIDTH,
                height = containerInnerHeight
            )
            stackHeight += containerInnerHeight

            blitk(
                matrixStack = matrices,
                texture = edgeResource,
                x = x + MARGIN,
                y = y + stackHeight,
                width = CONTAINER_WIDTH,
                height = 1
            )
            stackHeight += 1

            //Footer
            blitk(
                matrixStack = matrices,
                texture = footerResource,
                x = x + MARGIN,
                y = y + stackHeight,
                width = CONTAINER_WIDTH,
                height = 1
            )

            //Buttons
            for (index in 0..<category.pokemon.size) {
                val row = index / 3
                val column = index % 3

                val startX = x + MARGIN + 5 + column * (CONTAINER_BUTTON_SIZE + 2)
                val startY = y + HEADER_HEIGHT + 1 + BUTTON_SEPERATION + row * (BUTTON_SEPERATION + CONTAINER_BUTTON_SIZE)

                val active = starterSelectionScreen.currentCategory == category && starterSelectionScreen.currentSelection == index
                val hovered = getEntryAtPosition(mouseX, mouseY) == this && mouseX in startX..(startX + CONTAINER_BUTTON_SIZE) && mouseY in startY..(startY + CONTAINER_BUTTON_SIZE)

                blitk(
                    matrixStack = matrices,
                    texture = if (active) buttonActiveResource else buttonResource,
                    x = startX,
                    y = startY,
                    width = CONTAINER_BUTTON_SIZE,
                    height = CONTAINER_BUTTON_SIZE,
                    textureHeight = CONTAINER_BUTTON_SIZE * 2,
                    vOffset = if (hovered) CONTAINER_BUTTON_SIZE else 0
                )

                matrices.pushPose()
                matrices.translate(startX + (CONTAINER_BUTTON_SIZE / 2.0), startY.toDouble() - 6, 0.0)
                matrices.scale(3.5F, 3.5F, 1F)

                val state = FloatingState()
                drawProfilePokemon(
                    species = category.pokemon[index].species.resourceIdentifier,
                    matrixStack = matrices,
                    rotation = Quaternionf().fromEulerXYZDegrees(Vector3f(13F, 35F, 0F)),
                    state = state,
                    scale = 4.5F,
                    partialTicks = 0F
                )
                matrices.popPose()
            }
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            val entryY = getRowTop(this)
            val hoveredButton = getButtonHovered(x, entryY, mouseX, mouseY)
            if (hoveredButton != -1) {
                starterSelectionScreen.selectPokemon(category, hoveredButton)
                Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.GUI_CLICK, 1.0F))
                return true
            }
            return false
        }

        private fun getButtonHovered(x: Int, y: Int, mouseX: Double, mouseY: Double): Int {
            for (index in 0..<category.pokemon.size) {
                val row = index / 3
                val column = index % 3

                val startX = x + MARGIN + 5 + column * (CONTAINER_BUTTON_SIZE + 2)
                val startY = y + HEADER_HEIGHT + 1 + BUTTON_SEPERATION + row * (BUTTON_SEPERATION + CONTAINER_BUTTON_SIZE)

                if (getEntryAtPosition(mouseX.toInt(), mouseY.toInt()) == this && mouseX.toInt() in startX..(startX + CONTAINER_BUTTON_SIZE) && mouseY.toInt() in startY..(startY + CONTAINER_BUTTON_SIZE)) {
                    return index
                }
            }
            return -1
        }
    }
}