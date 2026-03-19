/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.snapshots

import com.cobblemon.mod.common.CobblemonBuildDetails
import net.minecraft.client.Minecraft
import net.minecraft.client.OptionInstance

import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Checkbox
import net.minecraft.client.gui.components.MultiLineTextWidget
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.GridLayout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

class SnapshotWarningScreen(val consumer: (Acknowledgement, Boolean) -> Unit) : Screen(TITLE) {

    companion object {

        private val TITLE = Component.translatable("cobblemon.snapshots.warning.title")
        private val DESCRIPTION = Component.translatable("cobblemon.snapshots.warning.description", CobblemonBuildDetails.VERSION)
        private val DONT_SHOW_AGAIN_TIP = Component.translatable("cobblemon.snapshots.warning.dont_show_again_tip")

    }

    private val layout = GridLayout().columnSpacing(10).rowSpacing(20)
    private val dontShowAgain = OptionInstance.createBoolean("cobblemon\$snapshots\$dont_show_again", false)

    override fun init() {
        super.init()

        val helper = this.layout.createRowHelper(2)
        val settings = helper.newCellSettings().alignHorizontallyCenter()

        helper.addChild(StringWidget(this.title, this.font), 2, settings)
        helper.addChild(MultiLineTextWidget(DESCRIPTION, this.font).setCentered(true).setMaxWidth(310), 2, settings)

        helper.addChild(Checkbox.builder(Component.translatable("cobblemon.snapshots.warning.dont_show_again"), Minecraft.getInstance().font)
            .selected(dontShowAgain)
            .tooltip(Tooltip.create(DONT_SHOW_AGAIN_TIP))
            .build(), 2, settings)

        helper.addChild(Button.builder(CommonComponents.GUI_YES) { this.consumer.invoke(Acknowledgement.YES, dontShowAgain.get()) }.build())
        helper.addChild(Button.builder(CommonComponents.GUI_NO) { this.consumer.invoke(Acknowledgement.NO, false) }.build())

        this.layout.visitWidgets { this.addRenderableWidget(it) }
        this.layout.arrangeElements()
        this.repositionElements()
    }

    override fun repositionElements() {
        FrameLayout.alignInRectangle(this.layout, 0, 0, this.width, this.height, 0.5F, 0.5F)
    }

    override fun onClose() {
        this.consumer.invoke(Acknowledgement.NO, false)
    }

    enum class Acknowledgement {
        YES,
        NO,
    }
}
