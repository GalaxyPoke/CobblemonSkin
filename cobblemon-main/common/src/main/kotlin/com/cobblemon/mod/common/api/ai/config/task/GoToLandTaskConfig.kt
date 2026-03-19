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
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.entity.OmniPathingEntity
import com.cobblemon.mod.common.util.closestPosition
import com.cobblemon.mod.common.util.mainThreadRuntime
import com.cobblemon.mod.common.util.withQueryValue
import kotlin.math.ceil
import kotlin.math.floor
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.level.pathfinder.PathComputationType

/**
 * Seeks land if it's underwater.
 */
class GoToLandTaskConfig : SingleTaskConfig {
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
                    if (!entity.isInWater) {
                        return@Trigger false
                    }

                    entity as PathfinderMob

                    mainThreadRuntime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
                    val walkSpeedValue = walkSpeed.resolveFloat(mainThreadRuntime)

                    val iterable = BlockPos.betweenClosed(
                        Mth.floor(entity.x - 8.0),
                        entity.blockY,
                        Mth.floor(entity.z - 8.0),
                        Mth.floor(entity.x + 8.0),
                        Mth.floor(entity.y + 2.0),
                        Mth.floor(entity.z + 8.0)
                    )

                    pathCooldown.setWithExpiry(true, 40L)

                    val blockPos = entity.closestPosition(iterable) {
                        isSafeLandPosAround(entity.level() as ServerLevel, it, entity)
                    } ?: return@Trigger false

                    walkTarget.set(WalkTarget(blockPos.above(), walkSpeedValue, 0))
                    return@Trigger true
                }
            }
        }
    }

    fun isSafeLandPos(world: ServerLevel, pos: BlockPos, mob: PathfinderMob): Boolean {
        val blockState = world.getBlockState(pos)

        val isFluid = world.getFluidState(pos.above()).isEmpty.not()
        val solidBelow = blockState.isSolid || (mob is OmniPathingEntity && mob.canFly())

        if (isFluid || !solidBelow) {
            return false
        } else {
            val mutable = BlockPos.MutableBlockPos()
            val minY = pos.y + 1
            val maxY = minY + ceil(mob.bbHeight).toInt()
            for (y in minY..maxY) {
                val aboveState = world.getBlockState(mutable.set(pos.x, y, pos.z))
                val canPathfindThroughAbove = aboveState.isPathfindable(PathComputationType.LAND)
                if (!canPathfindThroughAbove) {
                    return false
                }
            }

            return true
        }
    }

    fun isSafeLandPosAround(world: ServerLevel, pos: BlockPos, mob: PathfinderMob): Boolean {
        val minX = pos.x
        val maxX = minX + floor(mob.bbWidth).toInt()
        val minZ = pos.z
        val maxZ = minZ + floor(mob.bbWidth).toInt()

        val mutable = BlockPos.MutableBlockPos()
        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                if (!isSafeLandPos(world, mutable.set(x, pos.y, z), mob)) {
                    return false
                }
            }
        }

        return true
    }
}