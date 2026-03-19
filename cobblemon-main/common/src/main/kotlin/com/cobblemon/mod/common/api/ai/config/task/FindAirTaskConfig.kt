/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.util.closestPosition
import com.cobblemon.mod.common.util.getIsSubmerged
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.pathfinder.PathComputationType

/**
 * Moves to the nearest surface it can find so it can breathe.
 */
class FindAirTaskConfig : SingleTaskConfig {
    val walkSpeed = numberVariable(SharedEntityVariables.MOVEMENT_CATEGORY, SharedEntityVariables.WALK_SPEED, 0.35).asExpressible()
    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): List<MoLangConfigVariable> {
        return listOf(walkSpeed).asVariables()
    }

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (entity !is PathfinderMob) {
            return null
        }
        behaviourConfigurationContext.addMemories(
            MemoryModuleType.WALK_TARGET,
            CobblemonMemories.PATH_COOLDOWN
        )
        return BehaviorBuilder.create {
            it.group(
                it.absent(MemoryModuleType.WALK_TARGET),
                it.absent(CobblemonMemories.PATH_COOLDOWN)
            ).apply(it) { walkTarget, pathCooldown ->
                Trigger { world, entity, time ->
                    if (!entity.getIsSubmerged() || entity.isInLava) {
                        return@Trigger false
                    }

                    val iterable = BlockPos.betweenClosed(
                        Mth.floor(entity.x - 4.0),
                        entity.blockY - 1,
                        Mth.floor(entity.z - 4.0),
                        Mth.floor(entity.x + 4.0),
                        Mth.floor(entity.y + 8.0),
                        Mth.floor(entity.z + 4.0)
                    )

                    pathCooldown.setWithExpiry(true, 20L)

                    val blockPos = entity.closestPosition(iterable) { isAirPos(entity.level(), it) }
                        ?: return@Trigger false

                    walkTarget.set(WalkTarget(blockPos.above(), 0.35F, 0))
                    return@Trigger true
                }
            }
        }
    }

    private fun isAirPos(world: LevelReader, pos: BlockPos): Boolean {
        val blockState = world.getBlockState(pos)
        val aboveState = world.getBlockState(pos.above())
        val notFluid = world.getFluidState(pos.above()).isEmpty || blockState.`is`(Blocks.BUBBLE_COLUMN)
        val canPathfindThroughAbove = aboveState.isPathfindable(PathComputationType.LAND)
        val solidBelow = !blockState.isPathfindable(PathComputationType.LAND)

        return notFluid && canPathfindThroughAbove && solidBelow
    }
}