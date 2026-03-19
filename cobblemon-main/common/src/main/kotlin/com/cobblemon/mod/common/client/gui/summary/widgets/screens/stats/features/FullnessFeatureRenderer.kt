/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary.widgets.screens.stats.features

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.summary.featurerenderers.BarSummarySpeciesFeatureRenderer
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.gui.GuiGraphics

class FullnessFeatureRenderer(
    val selectedPokemon: Pokemon
) : BarSummarySpeciesFeatureRenderer(
    "fullness",
    lang("ui.stats.fullness"),
    cobblemonResource("textures/gui/summary/summary_stats_other_bar.png"),
    cobblemonResource("textures/gui/summary/summary_stats_fullness_overlay.png"),
    selectedPokemon,
    0,
    selectedPokemon.getMaxFullness(),
    selectedPokemon.currentFullness
) {
    override fun render(guiGraphics: GuiGraphics, x: Float, y: Float, pokemon: Pokemon): Boolean {
        renderElement(guiGraphics, x, y, pokemon)
        return true
    }

    override fun renderBar(guiGraphics: GuiGraphics, x: Float, y: Float, barValue: Int, barRatio: Float, barWidth: Int) {
        val (red, green, blue) = when {
            barRatio <= 0.33 -> Triple(120F/255F, 200F/255F, 80F/255F) // Green
            barRatio <= 0.66 -> Triple(240F/255F, 200F/255F, 65F/255F) // Yellow
            else -> Triple(230F/255F, 80F/255F, 65F/255F) // Red
        }

        blitk(
            matrixStack = guiGraphics.pose(),
            texture = CobblemonResources.WHITE,
            x = x + 3,
            y = y + 13,
            height = 10,
            width = barWidth,
            red = red,
            green = green,
            blue = blue
        )
    }
}
