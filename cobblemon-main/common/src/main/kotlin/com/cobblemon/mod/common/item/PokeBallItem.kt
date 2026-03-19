/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.util.isServerSide
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.Rarity
import net.minecraft.world.level.Level
import kotlin.math.cos

class PokeBallItem(
        val pokeBall: PokeBall
) : CobblemonItem(Properties().apply {
    when (pokeBall.name) {
        // Master balls are a netherite product and should be fire immune
        PokeBalls.MASTER_BALL.name -> fireResistant().rarity(Rarity.EPIC)
        PokeBalls.CHERISH_BALL.name -> rarity(Rarity.EPIC)
        PokeBalls.ANCIENT_ORIGIN_BALL.name -> rarity(Rarity.EPIC)
        PokeBalls.BEAST_BALL.name -> rarity(Rarity.RARE)
    }
}) {

    override fun use(world: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val itemStack = player.getItemInHand(usedHand)
        if (world.isServerSide()) {
            throwPokeBall(world, player as ServerPlayer)
        }
        itemStack.consume(1, player)
        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide)
    }

    private fun throwPokeBall(world: Level, player: ServerPlayer) {
        val pokeBallEntity = EmptyPokeBallEntity(pokeBall, player.level(), player).apply {
            val overhandFactor: Float = if (player.xRot < 0) {
                5f * cos(player.xRot.toRadians())
            } else {
                5f
            }

            shootFromRotation(player, player.xRot - overhandFactor, player.yRot, 0.0f, pokeBall.throwPower, 1.0f)
            setPos(position().add(deltaMovement.normalize().scale(1.0)))
            owner = player
        }
        world.addFreshEntity(pokeBallEntity)
    }
}