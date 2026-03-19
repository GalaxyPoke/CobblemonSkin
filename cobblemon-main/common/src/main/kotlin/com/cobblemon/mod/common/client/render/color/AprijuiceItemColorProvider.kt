/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.color

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.cooking.getColourMixFromRideStatBoosts
import net.minecraft.client.color.item.ItemColor
import net.minecraft.world.item.ItemStack

object AprijuiceItemColorProvider : ItemColor {

    private const val JUICE_INDEX = 1

    override fun getColor(stack: ItemStack, layer: Int): Int {
        if (layer == 0) return -1

        // todo we are not coloring the leaf anymore
        /*if (layer == LEAF_INDEX) {
            val quality = flavourComponent.getQuality()
            val color = when (quality) {
                CookingQuality.LOW -> ChatFormatting.RED.color
                CookingQuality.MEDIUM -> ChatFormatting.YELLOW.color
                CookingQuality.HIGH -> ChatFormatting.GREEN.color
            }

            color?.let { return FastColor.ARGB32.opaque(color) }
        }*/

        if (layer == JUICE_INDEX) {
            val rideBoostsComponent = stack.get(CobblemonItemComponents.RIDE_BOOST) ?: return -1
            if (rideBoostsComponent.boosts.isEmpty()) return -1
            val highestBoost = rideBoostsComponent.boosts.maxOf { it.value }
            val highestRidingStats = rideBoostsComponent.boosts.filter { it.value == highestBoost }.keys
            if (highestRidingStats.isEmpty()) return -1
            val colorMix = getColourMixFromRideStatBoosts(highestRidingStats)
            if (colorMix != null) return colorMix
        }

        return -1
    }
}