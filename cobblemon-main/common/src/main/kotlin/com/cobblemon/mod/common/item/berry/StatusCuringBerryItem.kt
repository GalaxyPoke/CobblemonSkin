/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.berry

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.api.pokemon.status.Status
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.block.BerryBlock
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

/**
 * A berry that cures statuses.
 *
 * @author Hiroku
 * @since August 4th, 2023
 */
class StatusCuringBerryItem(block: BerryBlock, vararg val status: Status): BerryItem(block), PokemonSelectingItem {
    override val bagItem = object : BagItem {
        override val itemName: String get() = "item.cobblemon.${this@StatusCuringBerryItem.berry()!!.identifier.path}"
        override val returnItem = Items.AIR
        override fun canUse(stack: ItemStack, battle: PokemonBattle, target: BattlePokemon) = canUseOnBattlePokemon(stack, target)
        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?) = "cure_status${status.takeIf { it.isNotEmpty() }?.let { " ${it.joinToString(separator = " ") { it.showdownName } }" } ?: "" }"
    }

    // check for whether a berry can cure a persistent status
    override fun canUseOnPokemon(stack: ItemStack, pokemon: Pokemon) = pokemon.status?.let { it.status in status || status.isEmpty() } == true &&
            pokemon.currentHealth > 0 &&
            super.canUseOnPokemon(stack, pokemon)

    // check for whether a berry can cure a volatile or persistent status in battle
    override fun canUseOnBattlePokemon(stack: ItemStack, battlePokemon: BattlePokemon): Boolean {
        battlePokemon.contextManager.get(BattleContext.Type.VOLATILE)?.map { it.id }?.let { volatiles ->  // TODO ContextManager is deprecated
            if (status.find { it.showdownName in volatiles } != null)
                return true
        }
        return this.canUseOnPokemon(stack, battlePokemon.effectedPokemon)
    }

    override fun applyToPokemon(
        player: ServerPlayer,
        stack: ItemStack,
        pokemon: Pokemon
    ): InteractionResultHolder<ItemStack>? {
        return if (canUseOnPokemon(stack, pokemon)) {
            pokemon.feedPokemon(1)
            pokemon.status = null
            stack.consume(1, player)
            InteractionResultHolder.success(stack)
        } else {
            InteractionResultHolder.fail(stack)
        }
    }

    override fun applyToBattlePokemon(player: ServerPlayer, stack: ItemStack, battlePokemon: BattlePokemon) {
        super.applyToBattlePokemon(player, stack, battlePokemon)
        battlePokemon.originalPokemon.feedPokemon(1)
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (user is ServerPlayer) {
            return use(user, user.getItemInHand(hand))
        }
        return super<BerryItem>.use(world, user, hand)
    }
}