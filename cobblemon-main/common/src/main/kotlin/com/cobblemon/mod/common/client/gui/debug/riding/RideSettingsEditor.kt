/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.debug.riding

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.CobblemonRideSettings
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.debug.ServerboundUpdateRidingSettingsPacket
import com.cobblemon.mod.common.net.messages.server.debug.ServerboundUpdateRidingStatsPacket
import com.cobblemon.mod.common.net.serverhandling.debug.ServerboundUpdateRidingSettingsHandler
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.getString
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.resolve
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import java.awt.Color

/**
 * Ride Settings Editor to change the underlying expressions and
 * stat mins and maxes of your ride on the fly (pun intended)
 *
 * Description
 *
 * @author Jackson
 * @since September 13, 2025
 */
class RideSettingsEditorGUI(val parentScreen: Screen, val vehicle: PokemonEntity, val rideStyle: RidingStyle, val rideSettings: RidingBehaviourSettings) : Screen(lang("ui.debug.riding_settings")), CobblemonRenderable {

    private val settingWidgets = mutableListOf<Pair<String, EditBox>>()
    private val originalValues = mutableMapOf<String, String>() // Used to keep track of saved state of editboxes

    override fun init() {
        super.init()
        clearWidgets()

        // Take note of the current saved values in the settings
        if (originalValues.isEmpty()) {
            val expressionVariables = getExpressionVariables(rideSettings)
            for ((name, value) in expressionVariables) {
                originalValues[name] = value
            }
        }

        addRenderableWidget(
            Button.builder("Back".text()) {
                this.minecraft?.setScreen(parentScreen)
            }.bounds(10, 10, 40, 20).build()
        )

        addRenderableWidget(
            Button.builder("Save".text()) { button ->
                saveRideSettings()
            }.bounds(300 + 110, 10, 40, 20).build()
        )

        createExpressionEditBoxes()

    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        // Make the background blank and transparent?
        guiGraphics.fill(0, 0, width, height, Color(0, 0, 0, 100).rgb)

        super.render(guiGraphics, mouseX, mouseY, partialTick)

        for ((name, editBox) in settingWidgets) {
            // Position the text next to the EditBox
            guiGraphics.drawString(font, name, editBox.x - 105, editBox.y + 5, 0xFFFFFF)
        }
    }


    private fun createExpressionEditBoxes() {
        val expressionVariables = getExpressionVariables(rideSettings)
        var currentY = 40
        for (variable in expressionVariables) {
            val (name, value) = variable

            val editBox = EditBox(this.minecraft!!.font, 130, currentY, 300, 20, name.text())
            editBox.setMaxLength(256)
            editBox.value = value

            // Dry run expression changes and if valid then turn white and if
            // invalid then turn red. If it remains the same it is green
            editBox.setResponder { newExpression ->
                when {
                    !validateExpression(newExpression) -> editBox.setTextColor(0xFF5555)
                    newExpression == originalValues[name] -> editBox.setTextColor(0x55FF55)
                    else -> editBox.setTextColor(0xFFFFFF)
                }
            }

            // Set all Boxes to green as they are currently saved
            editBox.setTextColor(0x55FF55)

            // Add widget
            addRenderableWidget(editBox)
            // Add widget to list
            settingWidgets.add(name to editBox)

            currentY += 25
        }
    }

    private fun saveRideSettings() {
        for ((name, editBox) in settingWidgets) {
            // If expression is valid then save and turn green
            if (validateExpression(editBox.value)) {
                ServerboundUpdateRidingSettingsHandler.modifyRideSettingsExpression(
                    vehicle,
                    rideStyle,
                    name,
                    editBox.value
                )
                // Update the running list of last saved values too
                originalValues[name] = editBox.value

                // Update server with
                CobblemonNetwork.sendToServer(
                    ServerboundUpdateRidingSettingsPacket(
                        vehicle.id,
                        rideStyle,
                        name,
                        editBox.value
                    )
                )
                editBox.setTextColor(0x55FF55)
            }
        }
    }

    private fun validateExpression(expression: String): Boolean {
        return try {
            // Attempt to parse as expression
            val parsedExpression = expression.asExpression()
            // Dry run to ensure its not borked.
            vehicle.runtime.resolve(parsedExpression)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getExpressionVariables(settings: RidingBehaviourSettings): List<Pair<String, String>> {
        val clazz = settings.javaClass
        val fields = clazz.declaredFields

        val gsField = CobblemonRideSettings.javaClass.declaredFields.firstOrNull { it.type == clazz }
        gsField?.isAccessible = true
        val controllerGlobalSettings = gsField?.get(CobblemonRideSettings) as? RidingBehaviourSettings?

        // Iterate through all expression fields and return the list of those that
        // aer expressions
        return fields.mapNotNull { field ->
            if (field.type == Expression::class.java) {
                field.isAccessible = true // Make private fields accessible
                val name = field.name
                // Get the value, cast it, and call .getString() on it
                if (field.get(settings) != null) {
                    val value = (field.get(settings) as Expression).getString()

                    // Create the pair and return it for the list
                    name to value

                } else {
                    // If the settings field is null then check for a global setting
                    val value = getMatchingGlobalSettingsValue(settings, controllerGlobalSettings, name) ?: "novalue"

                    name to value
                }
            } else {
                // If it's not an Expression (they all honestly should be if being used by the
                // behaviour in question) then ignore
                null
            }
        }
    }

    private fun getMatchingGlobalSettingsValue(settings: RidingBehaviourSettings, globalSettings: RidingBehaviourSettings?, fieldName: String): String? {
        if (globalSettings == null) return null
        val gssField = globalSettings.javaClass.declaredFields.firstOrNull {
            it.name == fieldName
        }
        gssField?.isAccessible = true
        return (gssField?.get(globalSettings) as? Expression)?.getString()
    }
}