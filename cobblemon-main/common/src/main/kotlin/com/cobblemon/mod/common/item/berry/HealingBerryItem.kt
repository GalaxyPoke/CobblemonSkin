/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.berry

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.item.HealingSource
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.genericRuntime
import com.cobblemon.mod.common.util.resolveInt
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

/**
 * A berry that heals a Pokémon by a fixed amount.
 *
 * @author Hiroku
 * @since August 4th, 2023
 */
class HealingBerryItem(block: BerryBlock, val amount: () -> ExpressionLike): BerryItem(block), PokemonSelectingItem, HealingSource {
    override val bagItem = object : BagItem {
        override val itemName: String get() = "item.cobblemon.${this@HealingBerryItem.berry()!!.identifier.path}"
        override val returnItem = Items.AIR
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?) = "potion ${ genericRuntime.resolveInt(amount(), battlePokemon) }"
        override fun canUse(stack: ItemStack, battle: PokemonBattle, target: BattlePokemon) =  target.health < target.maxHealth && target.health > 0
    }

    override fun canUseOnPokemon(stack: ItemStack, pokemon: Pokemon) = !pokemon.isFainted() && !pokemon.isFullHealth()
            && super.canUseOnPokemon(stack, pokemon)

    override fun applyToPokemon(
        player: ServerPlayer,
        stack: ItemStack,
        pokemon: Pokemon
    ): InteractionResultHolder<ItemStack>? {
        if (!canUseOnPokemon(stack, pokemon)) {
            return InteractionResultHolder.fail(stack)
        }

        pokemon.feedPokemon(1)
        pokemon.currentHealth = Integer.min(pokemon.currentHealth + genericRuntime.resolveInt(amount(), pokemon), pokemon.maxHealth)
        stack.consume(1, player)
        return InteractionResultHolder.success(stack)
    }

    override fun applyToBattlePokemon(player: ServerPlayer, stack: ItemStack, battlePokemon: BattlePokemon) {
        super.applyToBattlePokemon(player, stack, battlePokemon)
        battlePokemon.originalPokemon.feedPokemon(1)
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (world is ServerLevel && user is ServerPlayer) {
            return use(user, user.getItemInHand(hand))
        }
        return super<BerryItem>.use(world, user, hand)
    }
}