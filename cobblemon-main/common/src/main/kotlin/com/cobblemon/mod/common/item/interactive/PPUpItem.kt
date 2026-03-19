/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.item.PokemonAndMoveSelectingItem
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.item.CobblemonItem
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.Rarity
import net.minecraft.world.level.Level

class PPUpItem(
    val amount: Int
) : CobblemonItem(Properties().apply {
    if (amount>1) rarity(Rarity.UNCOMMON)
}), PokemonAndMoveSelectingItem {

    override val bagItem = null
    override fun canUseOnPokemon(stack: ItemStack, pokemon: Pokemon) = pokemon.moveSet.any { canUseOnMove(stack, it) }
    override fun canUseOnMove(stack: ItemStack, move: Move) = move.raisedPpStages < 3
    override fun applyToPokemon(
        player: ServerPlayer,
        stack: ItemStack,
        pokemon: Pokemon,
        move: Move
    ) {
        if (move.raiseMaxPP(amount)) {
            stack.consume(1, player)
            pokemon.entity?.playSound(CobblemonSounds.MEDICINE_PILLS_USE, 1F, 1F)
        }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (user is ServerPlayer) {
            use(user, user.getItemInHand(hand))?.let { return it }
        }
        return InteractionResultHolder.success(user.getItemInHand(hand))
    }
}