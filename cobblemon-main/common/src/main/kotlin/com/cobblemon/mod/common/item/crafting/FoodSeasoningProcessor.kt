/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.crafting

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.cooking.FoodUtils
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.item.components.FoodComponent
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.effect.MobEffectInstance

object FoodSeasoningProcessor : SeasoningProcessor {
    override val type = "food"

    override fun apply(result: ItemStack, seasoning: List<ItemStack>) {
        val foodData = result.get(DataComponents.FOOD)
        val baseHunger = foodData?.nutrition ?: 0
        val baseSaturation = foodData?.saturation ?: 0f

        val foodComponents = seasoning.mapNotNull {
            Seasonings.getFromItemStack(it)?.food?.toComponent()
        }

        if (foodComponents.isNotEmpty()) {
            val merged = FoodUtils.merge(foodComponents, extraHunger = baseHunger, extraSaturation = baseSaturation)
            result.set(CobblemonItemComponents.FOOD, merged)
        } // we still want there to be food data on the output item still? To be conistent? idk
        else {
            result.set(CobblemonItemComponents.FOOD, FoodComponent(baseHunger, baseSaturation))
        }
    }

    override fun consumesItem(seasoning: ItemStack): Boolean {
        val seasoningData = Seasonings.getFromItemStack(seasoning)
        return seasoningData?.food != null
    }
}