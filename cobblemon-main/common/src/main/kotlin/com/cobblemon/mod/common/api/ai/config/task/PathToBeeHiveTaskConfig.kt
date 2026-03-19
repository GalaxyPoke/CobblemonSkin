/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.CobblemonSensors
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.tasks.PlaceHoneyInHiveTask
import com.cobblemon.mod.common.util.distanceTo
import com.cobblemon.mod.common.util.getMemorySafely
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.phys.Vec3

class PathToBeeHiveTaskConfig : SingleTaskConfig {
    companion object {
        const val HONEY = "honey"
        const val STAY_OUT_OF_HIVE_COOLDOWN = 400

    }

    val condition = booleanVariable(HONEY, "can_add_honey", true).asExpressible()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(
        condition,
    ).asVariables()

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (entity !is PokemonEntity) {
            return null
        }
        if (!checkCondition(behaviourConfigurationContext.runtime, condition)) {
            return null
        }
        behaviourConfigurationContext.addSensors(CobblemonSensors.NEARBY_BEE_HIVE)
        return object : Behavior<LivingEntity>(
            mapOf(
                MemoryModuleType.LOOK_TARGET to MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET to MemoryStatus.VALUE_ABSENT,
                CobblemonMemories.HAS_NECTAR to MemoryStatus.REGISTERED,
                CobblemonMemories.HIVE_LOCATION to MemoryStatus.VALUE_PRESENT,
                CobblemonMemories.HIVE_COOLDOWN to MemoryStatus.VALUE_ABSENT,
                CobblemonMemories.HIVE_BLACKLIST to MemoryStatus.REGISTERED,
            ),
            400,
            400
        ) {

            val maxTicks = 400
            var traveledTicks = 0

            override fun checkExtraStartConditions(level: ServerLevel, owner: LivingEntity): Boolean {
                return (owner is PathfinderMob && owner.isAlive  && (owner is PokemonEntity && PlaceHoneyInHiveTask.wantsToEnterHive(owner))) && owner.brain.checkMemory(
                    MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT)
            }

            override fun canStillUse(level: ServerLevel, entity: LivingEntity, gameTime: Long): Boolean {
                if (maxTicks >= traveledTicks) return false
                val pos = entity.brain.getMemory(CobblemonMemories.HIVE_LOCATION)
                return !pos.isEmpty && entity.distanceTo(pos.get()) > 1.0
            }

            override fun start(level: ServerLevel, entity: LivingEntity, gameTime: Long) {
                super.start(level, entity, gameTime)
                val hiveLocation = entity.brain.getMemorySafely(CobblemonMemories.HIVE_LOCATION).orElse(null)
                val world = entity.level()
                val sidesByClosest = listOf(
                    hiveLocation.north(),
                    hiveLocation.south(),
                    hiveLocation.east(),
                    hiveLocation.west()
                ).sortedBy { it.distSqr(entity.blockPosition()) }

                var openSide = BlockPos.ZERO
                for (side in sidesByClosest) {
                    if (world.getBlockState(side).isAir) {
                        openSide = side
                        break
                    }
                }
                if (openSide == BlockPos.ZERO) {
                    if (world.getBlockState(hiveLocation.above()).isAir) {
                        // If the above position is also air, we can use it
                        openSide = hiveLocation.above() // Fallback to above if no open sides found
                    } else if (world.getBlockState(hiveLocation.below()).isAir) {
                        // If the below position is also air, we can use it
                        openSide = hiveLocation.below() // Fallback to below if no open sides found
                    } else {
                        // If no open sides or above/below positions are found, we cannot proceed
                        return
                    }
                }
                val targetVec = Vec3.atCenterOf(openSide)

                // Set path target toward hive
                if (pathfindDirectlyTowards(openSide, entity)) {
                    entity.brain.setMemory(MemoryModuleType.WALK_TARGET,WalkTarget(targetVec, 0.35F, 0))
                    entity.brain.setMemory(MemoryModuleType.LOOK_TARGET,BlockPosTracker(targetVec.add(0.0, entity.eyeHeight.toDouble(), 0.0)))
                    traveledTicks = 0
                } else {
                    dropAndBlackListHive(entity)
                }
            }

            override fun tick(level: ServerLevel, owner: LivingEntity, gameTime: Long) {
                traveledTicks++
            }

            override fun stop(level: ServerLevel, entity: LivingEntity, gameTime: Long) {
                if (maxTicks < traveledTicks) {
                    dropAndBlackListHive(entity)
                    entity.brain.setMemoryWithExpiry(CobblemonMemories.HIVE_COOLDOWN, true, 100)
                }
            }

            fun dropAndBlackListHive(entity: LivingEntity) {
                val hivePos = entity.brain.getMemorySafely(CobblemonMemories.HIVE_LOCATION).orElse(null)
                if (hivePos == null) return
                val blackList = entity.brain.getMemorySafely(CobblemonMemories.HIVE_BLACKLIST).orElse(emptyList()).toMutableList()

                blackList.add(hivePos)
                if (blackList.count() > 3) {
                    blackList.removeFirst()
                }
                entity.brain.eraseMemory(CobblemonMemories.HIVE_LOCATION)
                entity.brain.setMemory(CobblemonMemories.HIVE_BLACKLIST, blackList)

            }

            private fun pathfindDirectlyTowards(pos: BlockPos, entity: LivingEntity): Boolean {
                if (entity !is PathfinderMob) return false
                val nav = entity.navigation
                val path = nav. createPath(pos, 0)
                return path != null && path.canReach()
            }
        }
    }
}