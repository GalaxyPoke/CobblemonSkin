/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.ai

import com.cobblemon.mod.common.entity.OmniPathingEntity
import net.minecraft.core.BlockPos
import net.minecraft.tags.FluidTags
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.util.GoalUtils
import net.minecraft.world.entity.ai.util.RandomPos
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.function.Supplier
import java.util.function.ToDoubleFunction

/*
*
*  This is a rewrite of Minecraft's LandRandomPos tuned for
*  water and lava walkers.
*  The only real difference is movePosUpOutOfSolid() which
*  moves the target pos out of liquids
*
* */
class CobblemonRandomSurfacePos {
    companion object {
        fun getPos(mob: PathfinderMob, radius: Int, verticalRange: Int): Vec3? {
            Objects.requireNonNull(mob)
            return getPos(
                mob,
                radius,
                verticalRange,
                 { pos: BlockPos? ->
                     if (pos != null) {
                         mob.getWalkTargetValue(pos).toDouble()
                     }
                     0.0
                 })
        }

        fun getPos(mob: PathfinderMob, radius: Int, yRange: Int, toDoubleFunction: ToDoubleFunction<BlockPos?>): Vec3? {
            val bl = GoalUtils.mobRestricted(mob, radius)
            return RandomPos.generateRandomPos(Supplier {
                val blockPos = RandomPos.generateRandomDirection(mob.getRandom(), radius, yRange)
                val blockPos2 = generateRandomPosTowardDirection(mob, radius, bl, blockPos)
                if (blockPos2 == null) null else movePosUpOutOfSolid(mob, blockPos2)
            }, toDoubleFunction)
        }

        fun getPosTowards(mob: PathfinderMob, radius: Int, yRange: Int, vectorPosition: Vec3): Vec3? {
            val vec3 = vectorPosition.subtract(mob.getX(), mob.getY(), mob.getZ())
            val bl = GoalUtils.mobRestricted(mob, radius)
            return getPosInDirection(mob, radius, yRange, vec3, bl)
        }
        private fun getPosInDirection(
            mob: PathfinderMob,
            radius: Int,
            yRange: Int,
            vectorPosition: Vec3,
            shortCircuit: Boolean
        ): Vec3? {
            return RandomPos.generateRandomPos(mob, Supplier {
                val blockPos: BlockPos? =
                    RandomPos.generateRandomDirectionWithinRadians(
                        mob.getRandom(),
                        radius,
                        yRange,
                        0,
                        vectorPosition.x,
                        vectorPosition.z,
                        (Math.PI.toFloat() / 2f).toDouble()
                    )
                if (blockPos == null) {
                    return@Supplier null
                } else {
                    val blockPos2: BlockPos? =
                        generateRandomPosTowardDirection(
                            mob,
                            radius,
                            shortCircuit,
                            blockPos
                        )
                    return@Supplier if (blockPos2 == null) null else movePosUpOutOfSolid(
                        mob,
                        blockPos2
                    )
                }

            })
        }

        fun generateRandomPosTowardDirection(
            mob: PathfinderMob,
            radius: Int,
            shortCircuit: Boolean,
            pos: BlockPos
        ): BlockPos? {
            val blockPos = RandomPos.generateRandomPosTowardDirection(mob, radius, mob.getRandom(), pos)
            return if (!GoalUtils.isOutsideLimits(blockPos, mob) && !GoalUtils.isRestricted(
                    shortCircuit,
                    mob,
                    blockPos
                ) && !GoalUtils.isNotStable(mob.getNavigation(), blockPos)
            ) blockPos else null
        }

        fun movePosUpOutOfSolid(mob: PathfinderMob, pos: BlockPos): BlockPos? {
            var pos = pos
            pos = RandomPos.moveUpOutOfSolid(
                pos,
                mob.level().maxBuildHeight
            ) { blockPos: BlockPos ->
                if (GoalUtils.isSolid(mob, blockPos)) true
                val fluidState = mob.level().getFluidState(blockPos)
                !fluidState.isEmpty && (mob !is OmniPathingEntity || ((mob.canWalkOnWater() && fluidState.type == FluidTags.WATER) || (mob.canWalkOnLava() && fluidState.type == FluidTags.LAVA)))
            }
            return if (!GoalUtils.hasMalus(mob, pos)) pos else null
        }
    }
}