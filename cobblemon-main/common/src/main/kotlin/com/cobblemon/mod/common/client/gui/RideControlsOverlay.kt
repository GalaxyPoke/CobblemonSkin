/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.riding.behaviour.types.air.*
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeState
import com.cobblemon.mod.common.api.riding.behaviour.types.land.HorseBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.land.MinekartBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.land.VehicleBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.liquid.BoatBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.liquid.BurstBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.liquid.DolphinBehaviour
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.player.Input
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import kotlin.math.max

class RideControlsOverlay : Gui(Minecraft.getInstance()) {

    companion object {
        private const val CONTROLS_FADE_FRAMES = 20F
        private const val BASELINE_FPS = 60F

        private const val HALF_SCALE = 0.5F

        private val mouseResource = cobblemonResource("textures/gui/riding/mouse.png")
        private val mouseArrowLeftResource = cobblemonResource("textures/gui/riding/mouse_arrow_left.png")
        private val mouseArrowRightResource = cobblemonResource("textures/gui/riding/mouse_arrow_right.png")
        private val mouseArrowUpResource = cobblemonResource("textures/gui/riding/mouse_arrow_up.png")
        private val mouseArrowDownResource = cobblemonResource("textures/gui/riding/mouse_arrow_down.png")

        private val keyUpResource = cobblemonResource("textures/gui/riding/key_up.png")
        private val keyDownResource = cobblemonResource("textures/gui/riding/key_down.png")
        private val keyLeftResource = cobblemonResource("textures/gui/riding/key_left.png")
        private val keyRightResource = cobblemonResource("textures/gui/riding/key_right.png")
        private val keyHorizontalDisabledResource = cobblemonResource("textures/gui/riding/key_horizontal_disabled.png")

        private val keySneakResource = cobblemonResource("textures/gui/riding/key_sneak.png")
        private val keyJumpResource = cobblemonResource("textures/gui/riding/key_jump.png")
        private val keySneakWideResource = cobblemonResource("textures/gui/riding/key_sneak_wide.png")
        private val keyJumpWideResource = cobblemonResource("textures/gui/riding/key_jump_wide.png")
    }

    private val screenExemptions: List<Class<out Screen>> = listOf(ChatScreen::class.java)

    private var controlsMaxDurationFrames = Cobblemon.config.displayControlSeconds * BASELINE_FPS
    private var controlsDurationFrames = controlsMaxDurationFrames
    private var controlsFadeFrames = CONTROLS_FADE_FRAMES

    private var lastMouseX = 0
    private var lastMouseY = 0

    var mouseOffsetX = 0F
    var mouseOffsetY = 0F

    var mouseMovedUp: Boolean? = null
    var mouseMovedRight: Boolean? = null

    var currentBehaviourKey: ResourceLocation? = null

    override fun render(context: GuiGraphics, tickCounter: DeltaTracker) {
        val minecraft = Minecraft.getInstance()

        // Hiding if a Screen is open and not exempt
        minecraft.screen?.let { screen ->
            if ((!screenExemptions.contains(screen.javaClass as Class<out Screen>))) return
        }

        if (minecraft.options.hideGui || minecraft.debugOverlay.showDebugScreen()) return

        minecraft.player?.let { player ->
            val riddenEntity = player.vehicle
            if (riddenEntity != null && riddenEntity is PokemonEntity) {
                // Only show controls to driver
                if (riddenEntity === player.controlledVehicle) {
                    renderControls(minecraft, context, tickCounter, riddenEntity)
                }
            } else {
                resetControlsOverlayState()
            }
        }
    }

    private fun renderControls(minecraft: Minecraft, context: GuiGraphics, tickCounter: DeltaTracker, riddenEntity: PokemonEntity) {
        // Update max duration if user has changed config
        val configDuration = Cobblemon.config.displayControlSeconds  * BASELINE_FPS
        if (controlsMaxDurationFrames != configDuration) {
            controlsMaxDurationFrames = configDuration
            resetControlsOverlayState()
        }

        // Don't render if duration config set to 0 or less
        if (controlsMaxDurationFrames <= 0F) return

        val rideBehaviourSettings = riddenEntity.ridingController?.context?.settings ?: return
        val ridingBehaviourState = riddenEntity.ridingController?.context?.state ?: return
        val rideBehaviourKey = rideBehaviourSettings.key

        // Update current ride behaviour if changed
        // If composite, get active behaviour instead
        if (rideBehaviourKey == CompositeBehaviour.KEY) {
            val activeBehaviour = (ridingBehaviourState as CompositeState).activeBehaviour.get()
            if (currentBehaviourKey != activeBehaviour) {
                currentBehaviourKey = activeBehaviour
                resetControlsOverlayState()
            }
        } else if (currentBehaviourKey != rideBehaviourKey) {
            currentBehaviourKey = rideBehaviourKey
            resetControlsOverlayState()
        }

        // Render elements if entity has ride behaviour and if display duration has not ended yet
        if (rideBehaviourSettings != null && controlsFadeFrames > 0) {
            val tickDelta = tickCounter.realtimeDeltaTicks.takeIf { !minecraft.isPaused } ?: 0F
            val updateInterval = (tickDelta / 20F) * BASELINE_FPS

            controlsDurationFrames = max(controlsDurationFrames - updateInterval, 0F)
            if (controlsDurationFrames == 0F && controlsFadeFrames > 0) controlsFadeFrames = max(controlsFadeFrames - updateInterval, 0F)

            var showVerticalMouse = true // Show moving mouse up and down with arrows
            var showHorizontalMouse = true //  Show moving mouse left and right with arrows
            var showSneakKey = true
            var showJumpKey = true
            var showMovementKeys = true
            var disableHorizontalMovementKeys = false // Disable left and right movement keys

            // Configure what controls to show for each behaviour
            when (currentBehaviourKey) {
                BirdBehaviour.KEY -> {}
                BoatBehaviour.KEY -> {
                    showVerticalMouse = false
                    showHorizontalMouse = false
                    showSneakKey = false
                }
                BurstBehaviour.KEY -> {}
                DolphinBehaviour.KEY -> {
                    disableHorizontalMovementKeys = true
                    showSneakKey = false
                    showJumpKey = false
                }
                GliderBehaviour.KEY -> {}
                HelicopterBehaviour.KEY -> {}
                HorseBehaviour.KEY -> {
                    showVerticalMouse = false
                    disableHorizontalMovementKeys = true
                    showSneakKey = false
                }
                HoverBehaviour.KEY -> {
                    showVerticalMouse = false
                }
                JetBehaviour.KEY -> {
                    showSneakKey = false
                    showJumpKey = false
                }
                MinekartBehaviour.KEY -> {
                    showVerticalMouse = false
                    showHorizontalMouse = false
                    showSneakKey = false
                }
                RocketBehaviour.KEY -> {
                    showVerticalMouse = false
                    showHorizontalMouse = false
                }
                VehicleBehaviour.KEY -> {}
            }

            val centerX = minecraft.window.guiScaledWidth / 2
            val centerY = minecraft.window.guiScaledHeight / 2

            renderMouseControls(
                context,
                !showMovementKeys && !showJumpKey && !showSneakKey,
                showHorizontalMouse,
                showVerticalMouse,
                centerX,
                centerY / 2,
                controlsFadeFrames / CONTROLS_FADE_FRAMES
            )
            renderKeyControls(
                context,
                !showHorizontalMouse && !showVerticalMouse,
                showMovementKeys,
                disableHorizontalMovementKeys,
                showJumpKey,
                showSneakKey,
                minecraft.player?.input,
                centerX,
                centerY / 2,
                controlsFadeFrames / CONTROLS_FADE_FRAMES
            )
        }
    }

    private fun resetControlsOverlayState() {
        if (controlsDurationFrames != controlsMaxDurationFrames) controlsDurationFrames = controlsMaxDurationFrames
        if (controlsFadeFrames != CONTROLS_FADE_FRAMES) controlsFadeFrames = CONTROLS_FADE_FRAMES
        if (mouseMovedUp != null) {
            mouseMovedUp = null
            lastMouseY = 0
            mouseOffsetY = 0F
        }
        if (mouseMovedRight != null) {
            mouseMovedRight = null
            lastMouseX = 0
            mouseOffsetX = 0F
        }
    }

    private fun calculateMousePosition() {
        val minecraft = Minecraft.getInstance()
        val scaledWidth = minecraft.window.guiScaledWidth
        val scaledHeight = minecraft.window.guiScaledHeight
        val screenWidth = minecraft.window.screenWidth
        val screenHeight = minecraft.window.screenHeight

        val mouseX = Mth.floor(minecraft.mouseHandler.xpos() * scaledWidth.toDouble() / screenWidth.toDouble())
        val mouseY = Mth.floor(minecraft.mouseHandler.ypos() * scaledHeight.toDouble() / screenHeight.toDouble())

        val deltaMouseX = mouseX - lastMouseX
        val deltaMouseY = mouseY - lastMouseY

        mouseMovedRight = if (deltaMouseX > 0) true else if (deltaMouseX < 0) false else null
        mouseMovedUp = if (deltaMouseY < 0) true else if (deltaMouseY > 0) false else null

        lastMouseX = mouseX
        lastMouseY = mouseY

        mouseMovedRight?.let { movedRight ->
            if (movedRight && mouseOffsetX < 3) mouseOffsetX += 0.5F
            else if (!movedRight && mouseOffsetX > -3) mouseOffsetX -= 0.5F
        }
        if (mouseMovedRight == null)  mouseOffsetX = 0F

        mouseMovedUp?.let { movedUp ->
            if (movedUp && mouseOffsetY > -3) mouseOffsetY -= 0.5F
            else if (!movedUp && mouseOffsetY < 3) mouseOffsetY += 0.5F
        }
        if (mouseMovedUp == null) mouseOffsetY = 0F
    }

    private fun renderMouseControls(context: GuiGraphics, centered: Boolean, showHorizontal: Boolean, showVertical: Boolean, posX: Int, posY: Int, opacity: Float) {
        if (showVertical || showHorizontal) {
            calculateMousePosition()

            val offsetX = if (centered) -45.5 else 0.0

            drawScaledText(
                context = context,
                text = "tutorial.look.title".asTranslated(),
                x = posX + 45.5 + offsetX,
                y = posY - 50,
                centered = true,
                shadow = true,
                opacity = opacity
            )

            if (showHorizontal) {
                blitk(
                    matrixStack = context.pose(),
                    texture = mouseArrowLeftResource,
                    x = posX + 15 + offsetX,
                    y = posY - 15,
                    width = 9,
                    height = 15,
                    textureHeight = 30,
                    vOffset = if (mouseMovedRight == false) 15 else 0,
                    alpha = opacity
                )

                blitk(
                    matrixStack = context.pose(),
                    texture = mouseArrowRightResource,
                    x = posX + 67 + offsetX,
                    y = posY - 15,
                    width = 9,
                    height = 15,
                    textureHeight = 30,
                    vOffset = if (mouseMovedRight == true) 15 else 0,
                    alpha = opacity
                )
            }

            if (showVertical) {
                blitk(
                    matrixStack = context.pose(),
                    texture = mouseArrowUpResource,
                    x = posX + 39 + offsetX,
                    y = posY - 38,
                    width = 13,
                    height = 11,
                    textureHeight = 22,
                    vOffset = if (mouseMovedUp == true) 11 else 0,
                    alpha = opacity
                )

                blitk(
                    matrixStack = context.pose(),
                    texture = mouseArrowDownResource,
                    x = posX + 39 + offsetX,
                    y = posY + 13,
                    width = 13,
                    height = 11,
                    textureHeight = 22,
                    vOffset = if (mouseMovedUp == false) 11 else 0,
                    alpha = opacity
                )
            }

            blitk(
                matrixStack = context.pose(),
                texture = mouseResource,
                x = (posX + 35) + offsetX + (if (showHorizontal) mouseOffsetX else 0F),
                y = posY - 22 + (if (showVertical) mouseOffsetY else 0F),
                width = 21,
                height = 30,
                alpha = opacity
            )
        }
    }

    private fun renderKeyControls(context: GuiGraphics, centered: Boolean, showMovement: Boolean, disableHorizontal: Boolean, showJump: Boolean, showSneak: Boolean, input: Input?, posX: Int, posY: Int, opacity: Float) {
        val offsetX = if (centered) 43.5 else 0.0

        if (showMovement) {
            val offsetY = if (showJump || showSneak) 0 else 15

            drawScaledText(
                context = context,
                text = "key.categories.movement".asTranslated(),
                x = posX - 43.5 + offsetX,
                y = posY - 50,
                centered = true,
                shadow = true,
                opacity = opacity
            )

            blitk(
                matrixStack = context.pose(),
                texture = keyUpResource,
                x = (posX - 50 + offsetX) / HALF_SCALE,
                y = (posY - 33 + offsetY) / HALF_SCALE,
                height = 28,
                width = 28,
                textureHeight = 56,
                vOffset = if (input?.up ?: false) 28 else 0,
                scale = HALF_SCALE,
                alpha = opacity
            )

            blitk(
                matrixStack = context.pose(),
                texture = keyDownResource,
                x = (posX - 48 + offsetX) / HALF_SCALE,
                y = (posY - 21 + offsetY) / HALF_SCALE,
                height = 28,
                width = 20,
                textureHeight = 56,
                vOffset = if (input?.down ?: false) 28 else 0,
                scale = HALF_SCALE,
                alpha = opacity
            )

            if (disableHorizontal) {
                blitk(
                    matrixStack = context.pose(),
                    texture = keyHorizontalDisabledResource,
                    x = (posX - 61 + offsetX) / HALF_SCALE,
                    y = (posY - 23 + offsetY) / HALF_SCALE,
                    height = 32,
                    width = 72,
                    scale = HALF_SCALE,
                    alpha = opacity
                )
            } else {
                blitk(
                    matrixStack = context.pose(),
                    texture = keyLeftResource,
                    x = (posX - 61 + offsetX) / HALF_SCALE,
                    y = (posY - 23 + offsetY) / HALF_SCALE,
                    height = 32,
                    width = 26,
                    textureHeight = 64,
                    vOffset = if (input?.left ?: false) 32 else 0,
                    scale = HALF_SCALE,
                    alpha = opacity
                )

                blitk(
                    matrixStack = context.pose(),
                    texture = keyRightResource,
                    x = (posX - 38 + offsetX) / HALF_SCALE,
                    y = (posY - 23 + offsetY) / HALF_SCALE,
                    height = 32,
                    width = 26,
                    textureHeight = 64,
                    vOffset = if (input?.right ?: false) 32 else 0,
                    scale = HALF_SCALE,
                    alpha = opacity
                )
            }
        }

        if (showJump || showSneak) {
            val offsetY = if (showMovement) 0 else -14
            var label: MutableComponent?

            if (showJump && showSneak) {
                label = "key.sneak".asTranslated().append("/").append("key.jump".asTranslated())
                blitk(
                    matrixStack = context.pose(),
                    texture = keySneakResource,
                    x = (posX - 73 + offsetX) / HALF_SCALE,
                    y = (posY - 1 + offsetY) / HALF_SCALE,
                    height = 32,
                    width = 56,
                    textureHeight = 64,
                    vOffset = if (input?.shiftKeyDown ?: false) 32 else 0,
                    scale = HALF_SCALE,
                    alpha = opacity
                )

                blitk(
                    matrixStack = context.pose(),
                    texture = keyJumpResource,
                    x = (posX - 41 + offsetX) / HALF_SCALE,
                    y = (posY - 1 + offsetY) / HALF_SCALE,
                    height = 32,
                    width = 56,
                    textureHeight = 64,
                    vOffset = if (input?.jumping ?: false) 32 else 0,
                    scale = HALF_SCALE,
                    alpha = opacity
                )
            }
            else {
                label = (if (showSneak) "key.sneak" else "key.jump").asTranslated()
                blitk(
                    matrixStack = context.pose(),
                    texture = if (showSneak) keySneakWideResource else keyJumpWideResource,
                    x = (posX - 61 + offsetX) / HALF_SCALE,
                    y = (posY - 1 + offsetY) / HALF_SCALE,
                    height = 32,
                    width = 72,
                    textureHeight = 64,
                    vOffset = if ((if (showSneak) input?.shiftKeyDown else input?.jumping) ?: false) 32 else 0,
                    scale = HALF_SCALE,
                    alpha = opacity
                )
            }

            if (label != null) {
                drawScaledText(
                    context = context,
                    text = label,
                    x = posX - 43.5 + offsetX,
                    y = posY + if (showMovement) 23 else (-50),
                    centered = true,
                    shadow = true,
                    opacity = opacity
                )
            }
        }
    }
}
