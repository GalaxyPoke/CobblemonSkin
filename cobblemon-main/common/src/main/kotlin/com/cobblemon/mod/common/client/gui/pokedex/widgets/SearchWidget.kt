/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pokedex.widgets

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.HALF_OVERLAY_HEIGHT
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.HALF_OVERLAY_WIDTH
import com.cobblemon.mod.common.client.gui.pokedex.PokedexGUIConstants.SCALE
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.renderer.RenderType
import net.minecraft.network.chat.Component

class SearchWidget(
    val posX: Number,
    val posY: Number,
    width: Int,
    height: Int,
    text: Component = "Search".text(),
    val update: () -> (Unit)
): EditBox(Minecraft.getInstance().font, posX.toInt(), posY.toInt(), width, height, text), CobblemonRenderable {

    companion object {
        private val backgroundOverlay = cobblemonResource("textures/gui/pokedex/pokedex_screen_search_overlay.png")
        private val searchIcon = cobblemonResource("textures/gui/pokedex/search_icon.png")
    }

    var focusedTime: Long = 0

    init {
        this.setMaxLength(23)
        this.setResponder { update.invoke() }
        focusedTime = Util.getMillis()
    }

    override fun setFocused(focused: Boolean) {
        if (focused) focusedTime = Util.getMillis()
        super.setFocused(focused)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (mouseX.toInt() in x..(x + width) && mouseY.toInt() in y..(y + height)) isFocused = true
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val matrices = context.pose()

        blitk(
            matrixStack = matrices,
            texture = backgroundOverlay,
            x = posX, y = posY,
            width = HALF_OVERLAY_WIDTH,
            height = HALF_OVERLAY_HEIGHT
        )

        blitk(
            matrixStack = matrices,
            texture = searchIcon,
            x = (posX.toInt() + 3) / SCALE,
            y = (posY.toInt() + 2) / SCALE,
            width = 14,
            height = 14,
            scale = SCALE
        )

        val showCursor = isFocused && ((Util.getMillis() - this.focusedTime) / 300L % 2L == 0L)
        val input = if (isFocused) "${value}${if ((cursorPosition == value.length) && showCursor) "_" else ""}".text()
            else (if(value.isEmpty()) lang("ui.pokedex.search") else value.text())
        val startX = posX.toInt() + 13
        val startY = posY.toInt() + 1
        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = input.bold(),
            x = startX,
            y = startY,
            shadow = true
        )

        if (showCursor && !value.isEmpty() && cursorPosition != value.length) {
            val startToCursorWidth = Minecraft.getInstance().font.width((input.getString(cursorPosition).text().bold()).font(CobblemonResources.DEFAULT_LARGE))
            context.fill(
                RenderType.guiTextHighlight(),
                startX + startToCursorWidth - 1,
                startY,
                startX + startToCursorWidth,
                startY + 9,
                -3092272
            )
        }
    }
}
