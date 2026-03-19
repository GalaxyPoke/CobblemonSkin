/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.CobblemonItemComponents
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level

class PonigiriItem : Item(
        Properties().stacksTo(64)
                .food(FoodProperties.Builder()
                        .nutrition(2)
                        .saturationModifier(0.55f)
                        .build())
) {
    override fun getName(stack: ItemStack): Component {

        val ingredients = stack.get(CobblemonItemComponents.INGREDIENT)
                ?.ingredientIds?.map { it.toString() } ?: emptyList()

        val hasSweetBerries = "minecraft:sweet_berries" in ingredients

        val nameKey = when {
            hasSweetBerries -> "item.cobblemon.jelly_doughnut"
            else -> "item.cobblemon.ponigiri"
        }

        return Component.translatable(nameKey)
    }

    override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
        val effects = stack.get(CobblemonItemComponents.FOOD)

        if (effects != null && !world.isClientSide) {
            if (user is Player) { // todo maybe we want to be able to let other mobs eat these? or modded creatures that have hunder? idk
                user.foodData.add(effects.hunger, effects.saturation)
            }
        }

        return super.finishUsingItem(stack, world, user)
    }

    override fun getUseAnimation(stack: ItemStack) = UseAnim.EAT

    override fun getEatingSound(): SoundEvent {
        return SoundEvents.GENERIC_EAT
    }
}