/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.crafting

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.cooking.Flavour
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.item.components.FlavourComponent
import net.minecraft.world.item.ItemStack

object FlavourSeasoningProcessor : SeasoningProcessor {
    override val type = "flavour"

    override fun apply(result: ItemStack, seasoning: List<ItemStack>) {
        val isPokePuff = false // result.`is`(CobblemonItems.POKE_PUFF)
        val flavours = mutableMapOf<Flavour, Int>()

        for (seasoningStack in seasoning) {
            val seasoningObj = Seasonings.getFromItemStack(seasoningStack) ?: continue

            val relevantFlavours = if (isPokePuff) {
                val maxValue = seasoningObj.flavours?.maxOfOrNull { it.value } ?: continue
                seasoningObj.flavours.filterValues { it == maxValue } // keep all tied max flavours
            } else {
                seasoningObj.flavours ?: emptyMap()
            }

            for ((flavour, value) in relevantFlavours) {
                flavours[flavour] = (flavours[flavour] ?: 0) + value
            }
        }

        result.set(CobblemonItemComponents.FLAVOUR, FlavourComponent(flavours))
    }

    override fun consumesItem(seasoning: ItemStack): Boolean {
        val seasoningData = Seasonings.getFromItemStack(seasoning)
        return seasoningData != null && !seasoningData.flavours.isNullOrEmpty()
    }
}