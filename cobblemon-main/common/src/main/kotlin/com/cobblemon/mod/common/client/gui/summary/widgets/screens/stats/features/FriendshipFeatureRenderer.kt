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

class FriendshipFeatureRenderer(
    val selectedPokemon: Pokemon
) : BarSummarySpeciesFeatureRenderer(
    "friendship",
    lang("ui.stats.friendship"),
    cobblemonResource("textures/gui/summary/summary_stats_other_bar.png"),
    cobblemonResource("textures/gui/summary/summary_stats_friendship_overlay.png"),
    selectedPokemon,
    0,
    255,
    selectedPokemon.friendship
) {
    override fun render(guiGraphics: GuiGraphics, x: Float, y: Float, pokemon: Pokemon): Boolean {
        renderElement(guiGraphics, x, y, pokemon)
        return true
    }

    override fun renderBar(guiGraphics: GuiGraphics, x: Float, y: Float, barValue: Int, barRatio: Float, barWidth: Int) {
        val red = 1
        val green: Number = if (barValue >= 160) 0.28 else 0.56
        val blue: Number = if (barValue >= 160) 0.4 else 0.64

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
