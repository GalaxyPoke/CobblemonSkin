/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.berry

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonMechanics
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.api.pokemon.stats.ItemEvSource
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.genericRuntime
import com.cobblemon.mod.common.util.resolveInt
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import kotlin.math.max

/**
 * A berry that raises friendship but lowers EVs in a particular stat.
 *
 * @author Hiroku
 * @since August 4th, 2023
 */
class FriendshipRaisingBerryItem(block: BerryBlock, val stat: Stat) : BerryItem(block), PokemonSelectingItem {
    override val bagItem = null

    override fun canUseOnPokemon(stack: ItemStack, pokemon: Pokemon) = (pokemon.evs.getOrDefault(stat) > 0 || pokemon.friendship < Cobblemon.config.maxPokemonFriendship)
            && super.canUseOnPokemon(stack, pokemon)

    override fun applyToPokemon(
        player: ServerPlayer,
        stack: ItemStack,
        pokemon: Pokemon
    ): InteractionResultHolder<ItemStack> {
        if (!canUseOnPokemon(stack, pokemon)) {
            return InteractionResultHolder.fail(stack)
        }

        val friendshipRaiseAmount = genericRuntime.resolveInt(CobblemonMechanics.berries.friendshipRaiseAmount, pokemon)

        val increasedFriendship = pokemon.incrementFriendship(friendshipRaiseAmount)

        val evLowerAmount = max(genericRuntime.resolveInt(CobblemonMechanics.berries.evLowerAmount), 0)
        val decreasedEVs = pokemon.evs.add(stat, -evLowerAmount, ItemEvSource(player, stack, pokemon)) != 0

        return if (increasedFriendship || decreasedEVs) {
            pokemon.feedPokemon(1)

            stack.consume(1, player)
            InteractionResultHolder.success(stack)
        } else {
            InteractionResultHolder.pass(stack)
        }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (world is ServerLevel && user is ServerPlayer) {
            return use(user, user.getItemInHand(hand))
        }
        return super<BerryItem>.use(world, user, hand)
    }
}