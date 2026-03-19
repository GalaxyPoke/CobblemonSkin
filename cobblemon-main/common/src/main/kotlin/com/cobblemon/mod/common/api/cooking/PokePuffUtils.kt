/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.cooking

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.item.PokePuffItem.Companion.DISLIKED_FLAVOR_MULTIPLIER
import com.cobblemon.mod.common.item.PokePuffItem.Companion.LIKED_FLAVOR_MULTIPLIER
import com.cobblemon.mod.common.pokemon.Nature
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack

object PokePuffUtils {

    fun calculateFriendshipChange(stack: ItemStack, nature: Nature): Int {
        val dominantFlavour = getDominantFlavour(stack) ?: return 0
        val isNeutralNature = nature.favouriteFlavour == nature.dislikedFlavour

        val (baseFriendship) = calculateBaseFriendship(stack)

        val multiplier = if (
            dominantFlavour == nature.favouriteFlavour ||
            (dominantFlavour == Flavour.MILD && isNeutralNature)
        ) LIKED_FLAVOR_MULTIPLIER else -DISLIKED_FLAVOR_MULTIPLIER

        return (baseFriendship / multiplier).toInt()
    }

    fun calculateBaseFriendship(stack: ItemStack): Pair<Double, Double> {
        val ingredientIds = stack.get(CobblemonItemComponents.INGREDIENT)?.ingredientIds ?: emptyList()
        var baseFriendship = 0.0

        for (id in ingredientIds) {
            val item = BuiltInRegistries.ITEM.get(id)
            val itemStack = ItemStack(item)
            val seasoning = Seasonings.getFromItemStack(itemStack) ?: continue
            val dominantValue = seasoning.flavours?.values?.maxOrNull() ?: 0
            baseFriendship += dominantValue
        }

        val ingredientStrings = ingredientIds.map { it.toString() }
        val hasSugar = "minecraft:sugar" in ingredientStrings
        val hasSweet = ingredientStrings.any { it.startsWith("cobblemon:") && it.endsWith("_sweet") }

        val multiplier = when {
            hasSugar && hasSweet -> 5.0
            hasSweet -> 2.25
            hasSugar -> 1.75
            else -> 1.0
        }

        val finalBaseFriendship = baseFriendship * multiplier

        return finalBaseFriendship to multiplier
    }

    fun getDominantFlavour(stack: ItemStack): Flavour? {
        val flavours = stack.get(CobblemonItemComponents.FLAVOUR)?.flavours ?: return null
        val max = flavours.values.maxOrNull() ?: return null
        val dominant = flavours.filterValues { it == max }.keys

        return when {
            dominant.isEmpty() -> null
            dominant.size > 1 -> Flavour.MILD
            else -> dominant.first()
        }
    }

    fun getFlavorNatureLabel(flavour: Flavour): String {
        return when (flavour) {
            Flavour.SPICY -> "attack"
            Flavour.DRY -> "special_attack"
            Flavour.SWEET -> "speed"
            Flavour.BITTER -> "special_defence"
            Flavour.SOUR -> "defence"
            Flavour.MILD -> "neutral"
        }
    }

    fun getFriendshipChangeLiked(stack: ItemStack): Int {
        val (baseFriendship) = calculateBaseFriendship(stack)
        return (baseFriendship / LIKED_FLAVOR_MULTIPLIER).toInt()
    }

    fun getFriendshipChangeDisliked(stack: ItemStack): Int {
        val (baseFriendship) = calculateBaseFriendship(stack)
        return (baseFriendship / DISLIKED_FLAVOR_MULTIPLIER).toInt()
    }
}