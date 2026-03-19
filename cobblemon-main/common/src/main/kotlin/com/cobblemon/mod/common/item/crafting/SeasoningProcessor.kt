/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.crafting

import net.minecraft.world.item.ItemStack

/**
 * A processor that will apply some kind of changes to a cooking result [ItemStack] depending
 * on the seasoning items that were used. Register new processors by adding to [processors].
 * These are referenced by string literals in cooking pot recipe JSONs.
 *
 * @author Hiroku
 * @since March 18th, 2025
 */
interface SeasoningProcessor {
    companion object {
        val processors = mutableMapOf<String, SeasoningProcessor>(
            IngredientSeasoningProcessor.type to IngredientSeasoningProcessor,
            BaitSeasoningProcessor.type to BaitSeasoningProcessor,
            FlavourSeasoningProcessor.type to FlavourSeasoningProcessor,
            FoodColourSeasoningProcessor.type to FoodColourSeasoningProcessor,
            FoodSeasoningProcessor.type to FoodSeasoningProcessor,
            MobEffectSeasoningProcessor.type to MobEffectSeasoningProcessor,
            RideBoostsSeasoningProcessor.type to RideBoostsSeasoningProcessor,
        )
    }

    val type: String
    fun apply(result: ItemStack, seasoning: List<ItemStack>)
    fun consumesItem(seasoning: ItemStack): Boolean
}