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
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import org.joml.Vector3f

class InteractWheelButton(
    private val iconResource: ResourceLocation?,
    private val secondaryIconResource: ResourceLocation? = null,
    private val orientation: Orientation,
    private val tooltipText: String?,
    x: Int,
    y: Int,
    private val isEnabled: Boolean,
    private val colour: () -> Vector3f?,
    onPress: OnPress,
    private val canHover: (Double, Double) -> Boolean
) : Button(x, y, getButtonSize(orientation).first, getButtonSize(orientation).second, Component.literal("Interact"), onPress, DEFAULT_NARRATION), CobblemonRenderable {

    companion object {
        const val ICON_SIZE = 32
        const val ICON_SCALE = 0.5F

        private val buttonResources = mutableMapOf(
            Orientation.NORTH to cobblemonResource("textures/gui/interact/interact_wheel_button_north.png"),
            Orientation.NORTHEAST to cobblemonResource("textures/gui/interact/interact_wheel_button_northeast.png"),
            Orientation.EAST to cobblemonResource("textures/gui/interact/interact_wheel_button_east.png"),
            Orientation.SOUTHEAST to cobblemonResource("textures/gui/interact/interact_wheel_button_southeast.png"),
            Orientation.SOUTH to cobblemonResource("textures/gui/interact/interact_wheel_button_south.png"),
            Orientation.SOUTHWEST to cobblemonResource("textures/gui/interact/interact_wheel_button_southwest.png"),
            Orientation.WEST to cobblemonResource("textures/gui/interact/interact_wheel_button_west.png"),
            Orientation.NORTHWEST to cobblemonResource("textures/gui/interact/interact_wheel_button_northwest.png")
        )

        // Width and height as a pair
        private fun getButtonSize(orientation: Orientation): Pair<Int, Int> {
            return when (orientation) {
                Orientation.WEST, Orientation.EAST -> Pair(27, 60)
                Orientation.NORTH, Orientation.SOUTH -> Pair(60, 27)
                Orientation.NORTHWEST, Orientation.NORTHEAST,
                Orientation.SOUTHEAST, Orientation.SOUTHWEST -> Pair(49, 49)
            }
        }
    }

    private var passedTicks = 0F
    private val blinkInterval = 35

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val matrices = context.pose()
        passedTicks += delta

        blitk(
            matrixStack = matrices,
            texture = buttonResources[orientation],
            x = x,
            y = y,
            width = width,
            height = height,
            vOffset = if (isHovered(mouseX.toFloat(), mouseY.toFloat()) && isEnabled) height else 0,
            textureHeight = height * 2,
            alpha = if (isEnabled) 1f else 0.4F
        )

        if(isEnabled && isHovered(mouseX.toFloat(), mouseY.toFloat())){
            tooltipText?.let {
                context.renderTooltip(Minecraft.getInstance().font, Component.translatable(it), mouseX, mouseY)
            }
        }

        if (iconResource != null) {
            val (iconX, iconY) = getIconPosition(orientation)
            val colour = this.colour() ?: Vector3f(1F, 1F, 1F)
            blitk(
                matrixStack = matrices,
                texture = iconResource,
                x = iconX,
                y = iconY,
                width = ICON_SIZE,
                height = ICON_SIZE,
                alpha = if (isEnabled) 1F else 0.2F,
                red = colour.x,
                green = colour.y,
                blue = colour.z,
                scale = ICON_SCALE
            )
        }

        if (passedTicks % blinkInterval < blinkInterval / 2) {
            if (secondaryIconResource != null) {
                val (iconX, iconY) = getIconPosition(orientation)
                val colour = this.colour() ?: Vector3f(1F, 1F, 1F)
                blitk(
                    matrixStack = matrices,
                    texture = secondaryIconResource,
                    x = iconX,
                    y = iconY - ICON_SIZE,
                    width = ICON_SIZE,
                    height = ICON_SIZE,
                    alpha = if (isEnabled) 1F else 0.4F,
                    red = colour.x,
                    green = colour.y,
                    blue = colour.z,
                    scale = ICON_SCALE
                )
            }
        }
    }

    private fun getIconPosition(orientation: Orientation): Pair<Float, Float> {
        return when (orientation) {
            Orientation.NORTH, Orientation.SOUTH -> Pair((x + 22F) / ICON_SCALE, (y + 5.5F) / ICON_SCALE)
            Orientation.WEST, Orientation.EAST -> Pair((x + 5.5F) / ICON_SCALE, (y + 22F) / ICON_SCALE)
            Orientation.NORTHWEST -> Pair((x + 14.5F) / ICON_SCALE, (y + 14.5F) / ICON_SCALE)
            Orientation.NORTHEAST -> Pair((x + 18.5F) / ICON_SCALE, (y + 14.5F) / ICON_SCALE)
            Orientation.SOUTHEAST -> Pair((x + 18.5F) / ICON_SCALE, (y + 18.5F) / ICON_SCALE)
            Orientation.SOUTHWEST -> Pair((x + 14.5F) / ICON_SCALE, (y + 18.5F) / ICON_SCALE)
        }
    }

    private fun getOverLapZone(orientation: Orientation): Pair<ScreenRectangle, ScreenRectangle> {
        return when (orientation) {
            Orientation.NORTH -> Pair(ScreenRectangle(x, y + 12, 6, 15), ScreenRectangle(x + 54, y + 12, 6, 15))
            Orientation.NORTHEAST -> Pair(ScreenRectangle(x, y, 6, 15), ScreenRectangle(x + 34, y + 43, 15, 6))
            Orientation.EAST -> Pair(ScreenRectangle(x, y, 15, 6), ScreenRectangle(x, y + 54, 15, 6))
            Orientation.SOUTHEAST -> Pair(ScreenRectangle(x, y + 34, 6, 15), ScreenRectangle(x + 34, y, 15, 6))
            Orientation.SOUTH -> Pair(ScreenRectangle(x, y, 6, 15), ScreenRectangle(x + 54, y, 6, 15))
            Orientation.SOUTHWEST -> Pair(ScreenRectangle(x, y, 15, 6), ScreenRectangle(x + 43, y + 34, 6, 15))
            Orientation.WEST -> Pair(ScreenRectangle(x + 12, y, 15, 6), ScreenRectangle(x + 12, y + 54, 15, 6))
            Orientation.NORTHWEST -> Pair(ScreenRectangle(x, y + 43, 15, 6), ScreenRectangle(x + 43, y, 6, 15))
        }
    }

    override fun playDownSound(soundManager: SoundManager) {}

    private fun isHovered(mouseX: Float, mouseY: Float): Boolean {
        val (first, second) = getOverLapZone(orientation)
        
        if (first.containsPoint(mouseX.toInt(), mouseY.toInt()) || second.containsPoint(mouseX.toInt(), mouseY.toInt())) return false
        val xMin = x.toFloat() + 1
        val xMax = xMin + width - 2
        val yMin = y.toFloat() + 1
        val yMax = yMin + height - 2
        return canHover(mouseX.toDouble(), mouseY.toDouble()) && mouseX in xMin..xMax && mouseY in yMin..yMax
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return if (isHovered(mouseX.toFloat(), mouseY.toFloat())) super.mouseClicked(mouseX, mouseY, button) else false
    }

    override fun getTooltip(): Tooltip? {
        tooltipText?.let { return Tooltip.create(Component.translatable(it)) } ?: return super.getTooltip()
    }
}
