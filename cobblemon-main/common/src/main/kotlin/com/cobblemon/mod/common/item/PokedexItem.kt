/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.pokedex.PokedexType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.isLookingAt
import com.cobblemon.mod.common.util.isServerSide
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import net.minecraft.client.player.LocalPlayer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB

class PokedexItem(val type: PokedexType): CobblemonItem(Item.Properties().stacksTo(1)) {

    override fun getUseAnimation(itemStack: ItemStack): UseAnim? = UseAnim.TOOT_HORN

    override fun getUseDuration(stack: ItemStack, user: LivingEntity): Int = 72000

    override fun use(
        world: Level,
        player: Player,
        usedHand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        val itemStack = player.getItemInHand(usedHand)

        if (player.isCrouching) {
            val hitPokemon = player.traceFirstEntityCollision(
                entityClass = PokemonEntity::class.java,
                maxDistance = player.entityInteractionRange().toFloat()
            )

            // If the player is looking at a Pokémon within range, block Pokedex usage
            if (hitPokemon != null && hitPokemon.isOwnedBy(player)) {
                return InteractionResultHolder.pass(itemStack)
            }
        }

        if (world.isClientSide && player is LocalPlayer) {
            CobblemonClient.pokedexUsageContext.type = type
        }
        if (player !is ServerPlayer) return InteractionResultHolder.consume(itemStack)
        //Disables breaking blocks and damaging entities
        player.startUsingItem(usedHand)
        return InteractionResultHolder.fail(itemStack)
    }

    override fun onUseTick(
        world: Level,
        user: LivingEntity,
        stack: ItemStack,
        remainingUseTicks: Int
    ) {
        if (world.isServerSide() && user is ServerPlayer && user.isChangingDimension) {
            user.stopUsingItem()
            return
        }

        if (world.isClientSide && user is LocalPlayer) {
            val usageContext = CobblemonClient.pokedexUsageContext
            val ticksInUse = getUseDuration(stack, user) - remainingUseTicks
            usageContext.useTick(user, ticksInUse, true)
        }
        super.onUseTick(world, user, stack, remainingUseTicks)
    }

    override fun releaseUsing(
        stack: ItemStack,
        world: Level,
        user: LivingEntity,
        remainingUseTicks: Int
    ) {
        // Check if the player is interacting with a Pokémon
        val range = if (user is Player) user.entityInteractionRange() else 5.0
        val entity = world.getEntities(user, AABB.ofSize(user.position(), range, range, range))
            .filter { user.isLookingAt(it, stepDistance = 0.1F) }
            .minByOrNull { it.distanceTo(user) } as? PokemonEntity?

        if (world.isClientSide && user is LocalPlayer) {
            val usageContext = CobblemonClient.pokedexUsageContext
            val ticksInUse = getUseDuration(stack, user) - remainingUseTicks
            usageContext.stopUsing(ticksInUse, entity?.exposedSpecies?.resourceIdentifier)
        }

        super.releaseUsing(stack, world, user, remainingUseTicks)
    }
}
