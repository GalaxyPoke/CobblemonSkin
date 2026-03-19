/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.startselection

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.ColourLibrary
import com.cobblemon.mod.common.api.gui.MultiLineLabelK
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.client.gui.ExitButton
import com.cobblemon.mod.common.client.gui.TypeIcon
import com.cobblemon.mod.common.client.gui.VariableObjectSelectionScrollbar
import com.cobblemon.mod.common.client.gui.startselection.widgets.CategoryList
import com.cobblemon.mod.common.client.gui.startselection.widgets.SelectionButton
import com.cobblemon.mod.common.client.gui.startselection.widgets.preview.StarterRoundabout
import com.cobblemon.mod.common.client.gui.summary.Summary.Companion.PARTY
import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.config.starter.RenderableStarterCategory
import com.cobblemon.mod.common.net.messages.server.SelectStarterPacket
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.toasts.Toast
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import java.io.FileNotFoundException

/**
 * Starterselection Screen Thingy
 *
 * @author Qu
 * @since 2022-06-18
 */
class StarterSelectionScreen(private val categories: List<RenderableStarterCategory>): Screen("cobblemon.ui.starter.title".asTranslated()), CobblemonRenderable {
    companion object {
        // Size of UI at scale 1
        private const val BASE_WIDTH = 239
        private const val BASE_HEIGHT = 197

        // Resources
        private val baseResource = cobblemonResource("textures/gui/starterselection/base.png")
        private val backgroundResource = cobblemonResource("textures/gui/starterselection/background.png")
        private val backgroundBallResource = cobblemonResource("textures/gui/starterselection/background_poke_ball.png")

        private val platformResource = cobblemonResource("textures/gui/starterselection/platform_base.png")
        private val platformShadow = cobblemonResource("textures/gui/pokedex/platform_shadow.png")
        private val typeSpacerResource = cobblemonResource("textures/gui/starterselection/type_spacer_single.png")
        private val ballResource = cobblemonResource("textures/item/poke_balls/poke_ball.png")
    }

    var currentSelection = 0
    lateinit var currentCategory: RenderableStarterCategory
    private lateinit var modelWidget: ModelWidget
    private lateinit var currentPokemon: RenderablePokemon

    var ticksElapsed = 0
    var currentBallBackgroundFrame = 0

    override fun renderBlurredBackground(delta: Float) {}

    override fun renderMenuBackground(context: GuiGraphics) {}

    override fun init() {
        super.init()
        // Hide toast once checkedStarterScreen was set, which happens during the opening of the starter screen.
        if (CobblemonClient.checkedStarterScreen) {
            if (CobblemonClient.overlay.starterToast.nextVisibility != Toast.Visibility.HIDE) {
                CobblemonClient.overlay.starterToast.nextVisibility = Toast.Visibility.HIDE
            }
        }

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        if (categories.isEmpty()) {
            Cobblemon.LOGGER.warn("Empty category list while opening StarterSelectionUI")
            return
        }

        currentCategory = categories.first()
        currentPokemon = currentCategory.pokemon[currentSelection]

        with(currentPokemon) {
            modelWidget = ModelWidget(
                pX = x + 6, pY = y + 17,
                pWidth = 118, pHeight = 100,
                pokemon = this,
                baseScale = 2.7f,
                playCryOnClick = true,
                offsetY = -12.0
            )
        }

        addRenderableWidget(modelWidget)

        val selectionButton = SelectionButton(
            pX = x + 13, pY = y + 180,
            pWidth = SelectionButton.BUTTON_WIDTH, pHeight = SelectionButton.BUTTON_HEIGHT
        ) {
            CobblemonNetwork.sendToServer(
                SelectStarterPacket(
                    categoryName = currentCategory.name,
                    selected = currentSelection
                )
            )
            playSound(CobblemonSounds.GUI_CLICK)
            onClose()
        }
        addRenderableWidget(selectionButton)

        // Add Exit Button
        addRenderableWidget(
            ExitButton(pX = x + 210, pY = y + 181) {
                playSound(CobblemonSounds.GUI_CLICK)
                onClose()
            }
        )

        val categoryList = CategoryList(
            paneWidth = 97, paneHeight = 145,
            categories = categories,
            listX = x + 134, listY = y + 27,
            starterSelectionScreen = this
        )
        addRenderableWidget(categoryList)

        addRenderableWidget(VariableObjectSelectionScrollbar(
            parent = categoryList,
            x = x + 231,
            y = y + 27,
            width = 3,
            height = 145
        ))
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val matrices = context.pose()
        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        //Background
        blitk(
            matrixStack = matrices,
            texture = backgroundResource,
            x = x + 6,
            y = y + 17,
            width = 118,
            height = 100
        )

        blitk(
            matrixStack = matrices,
            texture = backgroundBallResource,
            x = x + 10.5,
            y = y + 12.5,
            width = 109,
            height = 109,
            textureHeight = 1744,
            vOffset = currentBallBackgroundFrame * 109
        )

        //Platform
        blitk(
            matrixStack = matrices,
            texture = platformResource,
            x = x + 8.5,
            y = y + 88,
            width = 113,
            height = 32
        )
        val typePlatform = getPlatformResource(currentPokemon.form.primaryType)
        if (typePlatform != null) {
            blitk(
                matrixStack = matrices,
                texture = typePlatform,
                x = x + 8.5,
                y = y + 82,
                width = 113,
                height = 30
            )
        }
        blitk(
            matrixStack = matrices,
            texture = platformShadow,
            x = (x + 43) / 0.5F,
            y = (y + 93.5) / 0.5F,
            width = 90,
            height = 20,
            scale = 0.5F
        )

        //Base
        blitk(
            matrixStack = matrices,
            texture = baseResource,
            x = x,
            y = y,
            width = BASE_WIDTH,
            height = BASE_HEIGHT
        )

        //Type Icon
        TypeIcon(
            x = x + 65,
            y = y + 120,
            type = currentPokemon.form.primaryType,
            secondaryType = currentPokemon.form.secondaryType,
            centeredX = true
        ).render(context)

        if (currentPokemon.form.secondaryType == null) {
            blitk(
                matrixStack = matrices,
                texture = typeSpacerResource,
                x = x + 47,
                y = y + 123,
                width = 36,
                height = 12
            )
        }

        //Name
        blitk(
            matrixStack = matrices,
            texture = ballResource,
            x = (x + 4) / 0.5F,
            y = (y + 3) / 0.5F,
            width = 16,
            height = 16,
            scale = 0.5F
        )

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = currentPokemon.species.translatedName.bold(),
            x = x + 14,
            y = y + 2,
            shadow = true
        )

        // Pokédex Number
        // Add preceding zeroes if Pokédex number is less than 4 digits
        var dexNo = currentPokemon.species.nationalPokedexNumber.toString()
        while (dexNo.length < 4) {
            dexNo = "0$dexNo"
        }
        dexNo = "#$dexNo"

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = dexNo.text().bold(),
            x = x + 79,
            y = y + 1,
            shadow = true
        )

        //"Title"
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.starter.title").bold(),
            x = x + 172,
            y = y + 11,
            shadow = true,
            centered = true
        )

        //Description Text
        val smallTextScale = 0.5F

        matrices.pushPose()
        matrices.scale(smallTextScale, smallTextScale, 1F)
        MultiLineLabelK.create(
            component = currentPokemon.form.pokedex.first().asTranslated(),
            width = 114 / smallTextScale,
            maxLines = 5
        ).renderLeftAligned(
            context = context,
            x = (x + 8) / smallTextScale,
            y = (y + 143) / smallTextScale,
            ySpacing = 5.5 / smallTextScale,
            colour = ColourLibrary.WHITE,
            shadow = true
        )
        matrices.popPose()

        super.render(context, mouseX, mouseY, delta)
    }

    fun getPlatformResource(type: ElementalType): ResourceLocation? {
        return try {
            cobblemonResource("textures/gui/starterselection/starter_platform_base_${type.showdownId}.png")
        } catch (error: FileNotFoundException) {
            null
        }
    }

    fun selectPokemon(category: RenderableStarterCategory, slot: Int) {
        currentCategory = category
        currentSelection = slot
        currentPokemon = currentCategory.pokemon[currentSelection].also {
            modelWidget.pokemon = it
        }
    }

    override fun tick() {
        ticksElapsed++

        val delay = 3
        if (ticksElapsed % delay == 0) currentBallBackgroundFrame++
        if (currentBallBackgroundFrame == 16) currentBallBackgroundFrame = 0
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (minecraft?.options?.keyInventory?.matches(keyCode, scanCode) == true) {
            Minecraft.getInstance().setScreen(null)
            return true
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun isPauseScreen() = true

    fun playSound(soundEvent: SoundEvent) {
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(soundEvent, 1.0F))
    }
}