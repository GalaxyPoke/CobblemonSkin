/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.getMemorySafely
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.level.block.BeehiveBlock
import net.minecraft.world.level.block.entity.BeehiveBlockEntity
import net.minecraft.world.phys.Vec3

object PlaceHoneyInHiveTask {

    fun create(): OneShot<PokemonEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.absent(MemoryModuleType.WALK_TARGET),
                it.registered(CobblemonMemories.HAS_NECTAR),
                it.present(CobblemonMemories.HIVE_LOCATION),
                it.absent(CobblemonMemories.HIVE_COOLDOWN),
            ).apply(it) { walkTarget, pollinated, hiveMemory, hiveCooldown ->
                Trigger { world, entity, time ->
                    if (entity !is PathfinderMob || !wantsToEnterHive(entity)) {
                        return@Trigger false
                    }

                    val hiveLocation = it.get(hiveMemory)
                    val targetVec = Vec3.atCenterOf(hiveLocation)

                    // if we are not close to it then end early
                    if (entity.distanceToSqr(targetVec) > 2.0) {
                        return@Trigger false
                    }

                    val state = world.getBlockState(hiveLocation)
                    val block = state.block
                    if (block is BeehiveBlock) {
                        val blockEntity = world.getBlockEntity(hiveLocation)
                        if (blockEntity !is BeehiveBlockEntity) return@Trigger false
                        if (blockEntity.isFull) {
                            // Erase Hive memory and give the bee a moment to locate a new hive
                            entity.brain.eraseMemory(CobblemonMemories.HIVE_LOCATION)
                        } else {
                            blockEntity.addOccupant(entity)
                        }
                    }

                    return@Trigger true
                }
            }
        }
    }

    fun wantsToEnterHive( entity: PokemonEntity) : Boolean {
        val result = !entity.brain.checkMemory(CobblemonMemories.HIVE_COOLDOWN, MemoryStatus.VALUE_PRESENT)
                && (entity.level().isRaining || entity.level().isNight || entity.brain.getMemory(CobblemonMemories.HAS_NECTAR).orElse(false))
        if (result) {
            val blockPos = entity.brain.getMemorySafely(CobblemonMemories.HIVE_LOCATION).orElse(null)
            val blockEntity = entity.level().getBlockEntity(blockPos)
            return blockEntity is BeehiveBlockEntity && !blockEntity.isFireNearby
        }
        return false
    }
}