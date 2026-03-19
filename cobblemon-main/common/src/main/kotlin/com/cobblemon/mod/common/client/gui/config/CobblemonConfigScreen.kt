/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.config

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.config.CobblemonConfig
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.ifServer
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.runOnServer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents

class CobblemonConfigScreen(
    private val previousScreen: Screen?,
) : Screen("gui.cobblemon_config.title".asTranslated()), CobblemonRenderable {

    companion object {
        const val HEIGHT = 240
        const val HEADER_HEIGHT = 45
        const val FOOTER_HEIGHT = 33
        const val PADDING = 4
        const val SLOT_WIDTH = 340
        const val SLOT_HEIGHT = 22
        const val HALF_SLOT_WIDTH = SLOT_WIDTH / 2
        const val RESET_SLOT_WIDTH = 40
        const val WIDGET_WIDTH = 110 - RESET_SLOT_WIDTH - PADDING
        const val WIDGET_HEIGHT = SLOT_HEIGHT - PADDING
        const val LABEL_Y_OFFSET = 4
        const val WIDGET_Y_OFFSET = 0
    }

    val clonedConfig = Cobblemon.config.clone()
    val defaultConfig = CobblemonConfig()
    val hideServerConfigs = Minecraft.getInstance().currentServer != null && !Minecraft.getInstance().isLocalServer

    private lateinit var layout: HeaderAndFooterLayout
    private lateinit var searchEdit: EditBox
    private lateinit var variableList: CobblemonConfigVariableList
    private lateinit var doneButton: Button

    private var searchString: String = ""

    override fun init() {
        super.init()

        if (::layout.isInitialized) layout.visitWidgets { removeWidget(it) }
        layout = HeaderAndFooterLayout(this, HEADER_HEIGHT, FOOTER_HEIGHT)

        if (::variableList.isInitialized) removeWidget(variableList)
        variableList = CobblemonConfigVariableList(this)
        variableList.filterConfigurations(searchString)
        addRenderableWidget(variableList)

        searchEdit = EditBox(
            Minecraft.getInstance().font,
            width / 2 - HALF_SLOT_WIDTH / 2,
            variableList.y,
            HALF_SLOT_WIDTH,
            SLOT_HEIGHT,
            lang("config.ui.search_configurations"),
        ).also {
            it.height = WIDGET_HEIGHT
            it.setMaxLength(250)

            it.value = searchString
            it.setResponder { text ->
                variableList.filterConfigurations(text)
                searchString = text
            }
        }

        val linearLayout = layout.addToHeader(LinearLayout.vertical())
        linearLayout.addChild(StringWidget(lang("config.ui.search_configurations"), this.font))
        linearLayout.addChild(searchEdit)
        layout.addToHeader(linearLayout)

        doneButton = Button.builder(CommonComponents.GUI_DONE) { doneButtonClick() }
            .pos(width / 2 - HALF_SLOT_WIDTH / 2, variableList.y + variableList.height + PADDING)
            .width(HALF_SLOT_WIDTH).build()
        layout.addToFooter(doneButton)

        layout.visitWidgets { widget ->
            addRenderableWidget(widget)
        }
        arrangeLayoutElements()
    }

    private fun arrangeLayoutElements() {
        layout.arrangeElements()
        variableList.updateSize(this.width, this.layout)
    }

    private fun doneButtonClick() {
        Cobblemon.saveConfig(clonedConfig)
        Cobblemon.reloadConfig()
        onClose()
    }

    override fun onClose() {
        minecraft?.setScreen(previousScreen)
    }
}