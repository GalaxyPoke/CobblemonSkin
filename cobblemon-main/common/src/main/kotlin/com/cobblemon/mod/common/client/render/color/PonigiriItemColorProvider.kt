/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.color

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.cooking.getColourMixFromColors
import com.cobblemon.mod.common.api.cooking.getColourMixFromFlavours
import com.cobblemon.mod.common.client.pot.CookingQuality
import net.minecraft.ChatFormatting
import net.minecraft.client.color.item.ItemColor
import net.minecraft.util.FastColor
import net.minecraft.world.item.ItemStack

object PonigiriItemColorProvider : ItemColor {

    private const val PONIGIRI_INDEX = 0

    override fun getColor(stack: ItemStack, layer: Int): Int {
        if (layer == 1 || layer == 2) return -1

        val colorComponent = stack.get(CobblemonItemComponents.FOOD_COLOUR) ?: return -1

        if (layer == PONIGIRI_INDEX) {
            val colorMix = getColourMixFromColors(colorComponent.getColoursAsARGB())
            if (colorMix != null) return colorMix
        }

        return -1
    }
}