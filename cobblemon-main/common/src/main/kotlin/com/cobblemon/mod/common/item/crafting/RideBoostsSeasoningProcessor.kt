/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.crafting

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.CobblemonMechanics
import com.cobblemon.mod.common.api.cooking.Flavour
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.api.riding.stats.RidingStat
import com.cobblemon.mod.common.item.AprijuiceItem
import com.cobblemon.mod.common.item.components.RideBoostsComponent
import com.cobblemon.mod.common.util.removeIf
import net.minecraft.world.item.ItemStack

/**
 * The seasoning processor that converts flavourful inputs and produces ride stat boosts on aprijuices.
 *
 * Example case:
 * - Berries with a total of 40 SPICY and 20 SWEET seasoning are applied to a Red Aprijuice.
 * - 40 SPICY hits the threshold for 2 points in ACCELERATION.
 * - 20 SWEET hits the threshold for 1 point in SPEED.
 * - The Red Aprijuice's inherent effects add 2 point to ACCELERATION and takes 1 point from STAMINA.
 * - The resulting RideBoostsComponent on the aprijuice will have:
 *   - ACCELERATION: 4 points (2 from SPICY + 2 from apricorn)
 *   - SPEED: 1 point (1 from SWEET)
 *   - STAMINA: -1 point (1 from apricorn)
 *
 * @author Hiroku
 * @since November 7th, 2025
 */
object RideBoostsSeasoningProcessor : SeasoningProcessor {
    override val type = "ride_boosts"

    override fun apply(result: ItemStack, seasoning: List<ItemStack>) {
        val flavours = mutableMapOf<Flavour, Int>()
        for (seasoningStack in seasoning) {
            val seasoningObj = Seasonings.getFromItemStack(seasoningStack) ?: continue
            for ((flavour, value) in seasoningObj.flavours ?: emptyMap()) {
                flavours[flavour] = (flavours[flavour] ?: 0) + value
            }
        }

        val resultItem = result.item
        if (resultItem is AprijuiceItem) {
            val apricornColour = resultItem.type
            val mechanic = CobblemonMechanics.aprijuices
            val apricornStatEffects = mechanic.apricornStatEffects[apricornColour] ?: emptyMap()

            val statBoosts = mutableMapOf<RidingStat, Int>()
            flavours.forEach { (flavour, value) ->
                val ridingStat = RidingStat.getByFlavour(flavour) ?: return@forEach
                val pointsFromFlavour = mechanic.statPointFlavourThresholds.filter { value >= it.key }.maxOfOrNull { it.value } ?: 0
                statBoosts[ridingStat] = pointsFromFlavour
            }
            RidingStat.entries.forEach { ridingStat ->
                val pointsFromApricorn = apricornStatEffects[ridingStat] ?: 0
                if (pointsFromApricorn != 0) {
                    statBoosts[ridingStat] = (statBoosts[ridingStat] ?: 0) + pointsFromApricorn
                }
            }

            statBoosts.removeIf { it.value == 0 }

            result.set(CobblemonItemComponents.RIDE_BOOST, RideBoostsComponent(statBoosts))
        }
    }

    override fun consumesItem(seasoning: ItemStack): Boolean {
        val seasoningData = Seasonings.getFromItemStack(seasoning)
        return seasoningData != null && !seasoningData.flavours.isNullOrEmpty()
    }
}