/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.item.CobblemonItem
import com.cobblemon.mod.common.pokemon.IVs
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

// An item that modifies a Pokémon's IVs according to a given modifier and valid range.
class HyperTrainingItem(val ivIncreaseAmount: Int, val targetStats: Set<Stat>, val validRange: IntRange) : CobblemonItem(Properties()), PokemonSelectingItem {

    override val bagItem = null

    // Helper to ensure prospective IVs are within the valid range
    private fun canChangeIV(stat: Stat, pokemon: Pokemon): Boolean {
        val effectiveIV = pokemon.ivs.getEffectiveBattleIV(stat)
        return effectiveIV in validRange && effectiveIV + ivIncreaseAmount in 0..IVs.MAX_VALUE
    }

    override fun canUseOnPokemon(stack: ItemStack, pokemon: Pokemon): Boolean {
        // Check if at least one stat's effective IV can be modified
        return targetStats.any { stat -> canChangeIV(stat, pokemon) }
    }

    override fun applyToPokemon(
        player: ServerPlayer,
        stack: ItemStack,
        pokemon: Pokemon
    ): InteractionResultHolder<ItemStack> {
        if(!canUseOnPokemon(stack, pokemon)) {
            return InteractionResultHolder.fail(stack)
        }
        // Modify the effective IVs for the target stats
        targetStats.forEach { stat ->
            if (canChangeIV(stat, pokemon)) {
                val effectiveIV = pokemon.ivs.getEffectiveBattleIV(stat)
                pokemon.hyperTrainIV(stat, effectiveIV + ivIncreaseAmount)
            }
        }

        stack.consume(1, player)
        pokemon.entity?.playSound(CobblemonSounds.MEDICINE_PILLS_USE, 1F, 1F)
        return InteractionResultHolder.success(stack)
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (user is ServerPlayer) {
            return use(user, user.getItemInHand(hand))
        }
        return InteractionResultHolder.success(user.getItemInHand(hand))
    }
}
