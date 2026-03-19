/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.api.text.blue
import com.cobblemon.mod.common.util.lang
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object SeasoningTooltipGenerator : TooltipGenerator() {
    override fun generateCategoryTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        val isSeasoningIngredient = Seasonings.isSeasoning(stack)
        val flavors = stack.get(CobblemonItemComponents.FLAVOUR)?.flavours
        val baitEffects = stack.get(CobblemonItemComponents.BAIT_EFFECTS)?.effects
        val mobEffects = stack.get(CobblemonItemComponents.MOB_EFFECTS)?.mobEffects
        val boosts = stack.get(CobblemonItemComponents.RIDE_BOOST)?.boosts

        val result = mutableListOf<Component>()

        if (!isSeasoningIngredient && flavors == null && baitEffects == null && mobEffects == null) return null // exit early if there is nothing to show to save time

        // This is where we label something as either an ingredient OR an item with Seasoning data. To help users know if something can be used IN the Pot or came FROM a pot at a glance

        if (isSeasoningIngredient) {
            // Flavor
            if (Seasonings.hasFlavors(stack)) result.add(flavorSeasoningHeader)

            // Food
            if (Seasonings.hasFood(stack)) result.add(foodSeasoningHeader)

            // Mob Effects
            if (Seasonings.hasMobEffect(stack)) result.add(mobEffectSeasoningHeader)

            // Bait Effects
            if (Seasonings.hasBaitEffects(stack)) result.add(baitEffectSeasoningHeader)
        } else { // these are output items that have Seasoning Component data on them
            // Flavor
            if (!flavors.isNullOrEmpty()) result.add(lang("seasoning_flavor_header").blue())

            // Mob Effects
            if (!mobEffects.isNullOrEmpty()) result.add(lang("seasoning_mob_effect_header").blue())

            // Bait Effects
            if (!baitEffects.isNullOrEmpty()) result.add(lang("seasoning_bait_effect_header").blue())

            if (!boosts.isNullOrEmpty()) {
                result.add(lang("seasoning_ride_boosts_info_header").blue())
            }
        }

        return if (result.isNotEmpty()) result else null
    }

    override fun generateAdditionalTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        val isSeasoningIngredient = Seasonings.isSeasoning(stack)
        val flavors = stack.get(CobblemonItemComponents.FLAVOUR)?.flavours
        val food = stack.get(CobblemonItemComponents.FOOD)
        val baitEffects = stack.get(CobblemonItemComponents.BAIT_EFFECTS)?.effects
        val rideBoosts = stack.get(CobblemonItemComponents.RIDE_BOOST)?.boosts

        if (!isSeasoningIngredient && flavors == null && food == null && baitEffects == null && rideBoosts == null) return null // exit early if there is nothing to show to save time

        val result = mutableListOf<Component>()

        // this is where we want to replace the headers with the info and data for each seasoning category we want to show

        // Flavor
        val flavorData = Seasonings.getFlavoursFromItemStack(stack).takeIf { !it.isNullOrEmpty() } ?: flavors

        if (flavorData != null && flavorData.any { it.value != 0 }) {
            result.addAll(generateAdditionalFlavorTooltip(flavorData))
        }

        // Bait Effects
        if (Seasonings.hasBaitEffects(stack) || baitEffects != null) {
            result.addAll(generateAdditionalBaitEffectTooltip(stack))
        }

        // Ride Boosts
        result.addAll(generateAdditionalRideBoostsTooltip(stack))

        return if (result.isNotEmpty()) result else null
    }
}
