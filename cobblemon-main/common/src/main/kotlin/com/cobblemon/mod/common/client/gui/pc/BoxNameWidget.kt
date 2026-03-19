/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.client.storage.ClientBox
import com.cobblemon.mod.common.net.messages.server.storage.pc.RequestRenamePCBoxPacket
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

class BoxNameWidget(
    pX: Int,
    pY: Int,
    text: Component = "BoxNameWidget".text(),
    private val pcGui: PCGUI,
    private val storageWidget: StorageWidget
): TextWidget(pX, pY, text = text, update = {}) {

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val label = getBox().name?: defaultBoxLabel()
        val input = applyTextCursor(value, label).bold()
        val textWidth = Minecraft.getInstance().font.width((if (isFocused) value.text() else label).bold().font(CobblemonResources.DEFAULT_LARGE))
        val centerX = x + (width / 2) - (textWidth / 2)
        if (startPosX != centerX) startPosX = centerX

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            x = startPosX,
            y = y + 3,
            text = input,
        )

        renderCursor(context, label)
    }

    override fun setFocused(focused: Boolean) {
        super.setFocused(focused)
        // Set text value to box's saved name if applicable on focus
        if (focused && value.isEmpty() && getBox().name != null) {
            value = getBox().name?.string ?: ""
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        // Reset box name
        if (isFocused && keyCode == InputConstants.KEY_ESCAPE) value = getBox().name?.string ?: ""
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun unfocused() {
        super.unfocused()
        RequestRenamePCBoxPacket(pcGui.pc.uuid, storageWidget.box, value).sendToServer()
        getBox().name = if (value.isBlank()) null else Component.literal(value).bold()
        value = ""
    }

    fun getBox(): ClientBox = pcGui.pc.boxes[storageWidget.box]

    fun defaultBoxLabel(): MutableComponent = Component.translatable("cobblemon.ui.pc.box.title", storageWidget.box + 1)
}
