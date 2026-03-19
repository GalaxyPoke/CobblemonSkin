/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.sensors.PokemonItemSensor.Companion.PICKUP_ITEMS
import com.cobblemon.mod.common.pokemon.ai.ObtainableItem
import com.cobblemon.mod.common.util.findMatchingEntry
import com.cobblemon.mod.common.util.getObjectList
import com.cobblemon.mod.common.util.mainThreadRuntime
import com.cobblemon.mod.common.util.resolve
import com.cobblemon.mod.common.util.withQueryValue
import com.google.common.collect.ImmutableMap
import net.minecraft.core.component.DataComponents
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.item.ItemStack


class EatHeldItemTask(entity: PokemonEntity) : Behavior<PokemonEntity>(
    ImmutableMap.of(
        MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
    )
) {

    companion object {
        private const val MAX_DURATION = 60
        private const val COOLDOWN = 120
    }

    private var timelastEaten: Long = 0
    val pickupItems = entity.config.getObjectList<ObtainableItem>(PICKUP_ITEMS)

    override fun checkExtraStartConditions(world: ServerLevel, entity: PokemonEntity): Boolean {
        if (timelastEaten + COOLDOWN > world.gameTime) {
            return false
        }
        val itemStack = entity.pokemon.heldItem()
        return !itemStack.isEmpty && canEat(itemStack, entity) && !entity.isBusy
    }

    override fun canStillUse(world: ServerLevel, entity: PokemonEntity, time: Long): Boolean {
        return !entity.pokemon.heldItem.isEmpty && !entity.pokemon.isFull() && !entity.isBusy
    }

    private fun canEat(item: ItemStack, entity: PokemonEntity): Boolean {
        return item.has(DataComponents.FOOD) || pickupItems.findMatchingEntry(entity.registryAccess(), item)?.onUseEffect != null
    }

    override fun start(world: ServerLevel, entity: PokemonEntity, time: Long) {
        timelastEaten = time
        entity.brain.setMemory(CobblemonMemories.IS_CONSUMING_ITEM, true)
    }

    override fun stop(level: ServerLevel, entity: PokemonEntity, gameTime: Long) {
        entity.brain.eraseMemory(CobblemonMemories.IS_CONSUMING_ITEM)
    }

    override fun tick(world: ServerLevel, entity: PokemonEntity, time: Long) {
        if (!world.isClientSide && entity.isAlive) {
            val itemStack: ItemStack = entity.pokemon.heldItem()
            val itemConfig = pickupItems.findMatchingEntry(entity.registryAccess(), itemStack)
            if (canEat(itemStack, entity)) {
                if (this.timelastEaten + MAX_DURATION <= time) {
                    var resultItemStack = itemStack.finishUsingItem(world, entity)
                    val configuredReturnItem = itemConfig?.returnItem
                    resultItemStack = if (configuredReturnItem != ItemStack.EMPTY) ItemStack(BuiltInRegistries.ITEM.get(configuredReturnItem))
                        else if (resultItemStack.item == itemStack.item)
                            ItemStack.EMPTY // Items that aren't normally edible return themselves
                        else
                            resultItemStack
                    entity.pokemon.swapHeldItem(resultItemStack)
                    val onUseEffect = itemConfig?.onUseEffect
                    if (onUseEffect != null) {
                        mainThreadRuntime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
                        mainThreadRuntime.resolve(onUseEffect)
                    }
                    this.timelastEaten = time
                    if (itemConfig != null && itemConfig.fullnessValue > 0) {
                        entity.pokemon.feedPokemon(itemConfig.fullnessValue, false)
                    }
                    // Check if the result item is something that should be dropped
                    resultItemStack = entity.pokemon.heldItem()
                    if ((pickupItems.findMatchingEntry(entity.registryAccess(), resultItemStack)?.pickupPriority ?: 0) < 0) {
                        // Drop item
                        resultItemStack = entity.pokemon.swapHeldItem(resultItemStack)
                        if (!resultItemStack.isEmpty && !entity.level().isClientSide) {
                            PickUpItemTask.dropItem(entity, resultItemStack)
                        }
                    }
                } else if ((itemConfig?.fullnessValue ?: 0) > 0 && this.timelastEaten > 0 && entity.random.nextFloat() < 0.4f) {
                    entity.playSound(entity.getEatingSound(itemStack), 1.0f, 1.0f)
                    world.broadcastEntityEvent(entity, 45.toByte())
                    spawnFoodParticles(entity, itemStack)
                }
            }

        }
    }

    private fun spawnFoodParticles(entity: LivingEntity, itemStack: ItemStack) {
        val serverLevel = entity.level() as ServerLevel
        //TODO: Figure out how to this with snowstorm so we can use mouth/face/head locators
        // The issue as of this writing is we don't have a clean way to utilize the item texture for the particle in snowstorm
        serverLevel.sendParticles(
            ItemParticleOption(ParticleTypes.ITEM, itemStack),
            entity.x, entity.y + 0.5, entity.z, // particle position (centered on entity)
            5, // count
            0.1, 0.1, 0.1, // x, y, z offset spread
            0.05 // speed
        )
    }


}