/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.interact.wheel

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.common.collect.Multimap
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import kotlin.math.max

class InteractWheelGUI(private val options: Multimap<Orientation, InteractWheelOption>, title: Component) : Screen(title), CobblemonRenderable {
    companion object {
        const val SIZE = 170
        private val backgroundResource = cobblemonResource("textures/gui/interact/interact_wheel_base.png")
    }

    private val buttons = mutableListOf<InteractWheelButton>()
    private var maxPage = 1
    private var currentPage = 0
    override fun renderBlurredBackground(delta: Float) { }
    override fun renderMenuBackground(context: GuiGraphics) {}

    override fun init() {
        calculateMaxPage()
        addButton(Orientation.NORTH, options[Orientation.NORTH].toList().getOrNull(0))
        addButton(Orientation.NORTHEAST, options[Orientation.NORTHEAST].toList().getOrNull(0))
        addButton(Orientation.EAST, options[Orientation.EAST].toList().getOrNull(0))
        addButton(Orientation.SOUTHEAST, options[Orientation.SOUTHEAST].toList().getOrNull(0))
        addButton(Orientation.SOUTH, options[Orientation.SOUTH].toList().getOrNull(0))
        addButton(Orientation.SOUTHWEST, options[Orientation.SOUTHWEST].toList().getOrNull(0))
        addButton(Orientation.WEST, options[Orientation.WEST].toList().getOrNull(0))
        addButton(Orientation.NORTHWEST, options[Orientation.NORTHWEST].toList().getOrNull(0))

        if (maxPage > 1) {
            val (x, y) = getBasePosition()
            addRenderableWidget(ArrowButton(
                pX = x - 14,
                pY = y + 78,
                isRight = false,
                onPress = {
                    // loop to last page if on page 0, otherwise go to previous page
                    setPage(if (currentPage == 0) maxPage - 1 else currentPage - 1)
                }
            ))
            addRenderableWidget(ArrowButton(
                pX = x + 175,
                pY = y + 78,
                isRight = true,
                onPress = { setPage((currentPage + 1) % max(1, maxPage)) }
            ))
        }
    }

    private fun calculateMaxPage() {
        maxPage = maxOf(
            options[Orientation.NORTH].size,
            options[Orientation.NORTHEAST].size,
            options[Orientation.EAST].size,
            options[Orientation.SOUTHEAST].size,
            options[Orientation.SOUTH].size,
            options[Orientation.SOUTHWEST].size,
            options[Orientation.WEST].size,
            options[Orientation.NORTHWEST].size
        )
    }

    private fun setPage(page: Int) {
        currentPage = page
        buttons.forEach { removeWidget(it) }
        buttons.clear()
        val orientations = Orientation.values()
        orientations.forEach { orientation ->
            val option = options[orientation].toList().getOrNull(page)
            addButton(orientation, option)
        }
    }

    private fun addButton(orientation: Orientation, option: InteractWheelOption?) {
        val (x, y) = getButtonPosition(orientation)
        buttons.add(
            addRenderableWidget(
                InteractWheelButton(
                    iconResource = option?.iconResource,
                    secondaryIconResource = option?.secondaryIconResource,
                    orientation = orientation,
                    tooltipText = option?.tooltipText,
                    x = x,
                    y = y,
                    isEnabled = option != null && option.enabled,
                    colour = option?.colour ?: { null },
                    onPress = { option?.onPress?.invoke() },
                    canHover = { a: Double, b: Double -> !isMouseInCenter(a, b)}
                )
            )
        )
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val (x, y) = getBasePosition()
        blitk(
            matrixStack = context.pose(),
            texture = backgroundResource,
            x = x,
            y = y,
            width = SIZE,
            height = SIZE
        )
        super.render(context, mouseX, mouseY, delta)
    }

    private fun getBasePosition(): Pair<Int, Int> {
        return Pair((width - SIZE) / 2, (height - SIZE) / 2)
    }

    private fun getButtonPosition(orientation: Orientation): Pair<Int, Int> {
        val (x, y) = getBasePosition()
        return when (orientation) {
            Orientation.NORTH -> Pair(x + 55, y)
            Orientation.NORTHEAST -> Pair(x + 109, y + 12)
            Orientation.EAST -> Pair(x + 143, y + 55)
            Orientation.SOUTHEAST -> Pair(x + 109, y + 109)
            Orientation.SOUTH -> Pair(x + 55, y + 143)
            Orientation.SOUTHWEST -> Pair(x + 12, y + 109)
            Orientation.WEST -> Pair(x, y + 55)
            Orientation.NORTHWEST -> Pair(x + 12, y + 12)
        }
    }

    override fun isPauseScreen() = false

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isMouseInCenter(mouseX, mouseY)) return false
        return super.mouseClicked(mouseX, mouseY, button)
    }

    private fun isMouseInCenter(mouseX: Double, mouseY: Double): Boolean {
        val (x, y) = getBasePosition()
        val centerX = (x + 44).toFloat()
        val xMax = centerX + 82
        val centerY = (y + 44).toFloat()
        val yMax = centerY + 82
        return mouseX.toFloat() in centerX..xMax && mouseY.toFloat() in centerY..yMax
    }
}
