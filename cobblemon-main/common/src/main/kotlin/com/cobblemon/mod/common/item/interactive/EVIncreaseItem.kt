/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.api.pokemon.stats.ItemEvSource
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.item.CobblemonItem
import com.cobblemon.mod.common.pokemon.EVs
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

abstract class EVIncreaseItem(
    val stat: Stat,
    val evIncreaseAmount: Int,
) : CobblemonItem(Properties()), PokemonSelectingItem {
    override val bagItem = null
    override fun canUseOnPokemon(stack: ItemStack, pokemon: Pokemon) = pokemon.evs.getOrDefault(stat) < EVs.MAX_STAT_VALUE &&
            super.canUseOnPokemon(stack, pokemon)

    abstract val sound: SoundEvent

    override fun applyToPokemon(
        player: ServerPlayer,
        stack: ItemStack,
        pokemon: Pokemon
    ): InteractionResultHolder<ItemStack> {
        val evsGained = pokemon.evs.add(stat, evIncreaseAmount, ItemEvSource(player, stack, pokemon))
        return if (evsGained > 0) {
            pokemon.entity?.playSound(sound, 1F, 1F)
            stack.consume(1, player)
            InteractionResultHolder.success(stack)
        } else {
            InteractionResultHolder.fail(stack)
        }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (user is ServerPlayer) {
            return use(user, user.getItemInHand(hand))
        }
        return InteractionResultHolder.success(user.getItemInHand(hand))
    }
}