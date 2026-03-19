/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.ai

import com.cobblemon.mod.common.pokemon.ai.OmniPathNodeMaker
import com.cobblemon.mod.common.util.deleteNode
import java.util.function.Predicate
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.entity.ai.util.DefaultRandomPos
import net.minecraft.world.level.pathfinder.Path
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.Vec3

class FollowWalkTargetTask(
    minRunTime: Int = 150,
    maxRunTime: Int = 250
) : Behavior<PathfinderMob>(
    mapOf(
        MemoryModuleType.WALK_TARGET to MemoryStatus.VALUE_PRESENT,
        MemoryModuleType.PATH to MemoryStatus.VALUE_ABSENT,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE to MemoryStatus.REGISTERED
    ), minRunTime, maxRunTime
) {
    private var pathUpdateCountdownTicks = 0
    private var path: Path? = null
    private var lookTargetPos: BlockPos? = null
    private var speed = 0f

    override fun checkExtraStartConditions(level: ServerLevel, entity: PathfinderMob): Boolean {
        if (this.pathUpdateCountdownTicks > 0) {
            --this.pathUpdateCountdownTicks
            return false
        } else {
            val brain = entity.brain
            val walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get()
            val hasReached = this.hasReached(entity, walkTarget)
            resetPathUpdateCountdownTicks(entity)
            if (!hasReached && this.hasFinishedPath(entity, walkTarget, level.gameTime)) {
                this.lookTargetPos = walkTarget.target.currentBlockPosition()
                return true
            } else {
                brain.eraseMemory(MemoryModuleType.WALK_TARGET)
                if (hasReached) {
                    brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
                }

                return false
            }
        }
    }

    fun resetPathUpdateCountdownTicks(entity: PathfinderMob) {
        pathUpdateCountdownTicks = entity.random.nextInt(20) + 20
    }

    override fun canStillUse(arg: ServerLevel, arg2: PathfinderMob, l: Long): Boolean {
        if (this.path != null && this.lookTargetPos != null) {
            val optional = arg2.brain.getMemory(MemoryModuleType.WALK_TARGET)
            val isTargetSpectator = optional.map(::isTargetSpectator).orElse(false)
            val entityNavigation = arg2.navigation
            return !entityNavigation.isDone && optional.isPresent && !this.hasReached(arg2, optional.get()) && !isTargetSpectator
        } else {
            return false
        }
    }

    override fun stop(world: ServerLevel, entity: PathfinderMob, l: Long) {
        val walkTarget = entity.brain.getMemory(MemoryModuleType.WALK_TARGET).orElse(null)
        if (walkTarget != null && !this.hasReached(entity, walkTarget) && entity.navigation.isStuck) {
            resetPathUpdateCountdownTicks(entity)
        }

        entity.navigation.stop()
        entity.brain.eraseMemory(MemoryModuleType.WALK_TARGET)
        entity.brain.eraseMemory(MemoryModuleType.PATH)
        this.path = null
    }

    override fun start(arg: ServerLevel, entity: PathfinderMob, time: Long) {
        entity.brain.setMemory(MemoryModuleType.PATH, this.path)
        entity.navigation.moveTo(this.path, speed.toDouble())
    }

    override fun tick(world: ServerLevel, entity: PathfinderMob, l: Long) {
        val path = entity.navigation.path
        val brain = entity.brain
        if (this.path !== path) {
            this.path = path
            brain.setMemory(MemoryModuleType.PATH, path)
        }

        if (path != null && lookTargetPos != null) {
            val walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get()
            if (walkTarget.target.currentBlockPosition().distSqr(lookTargetPos!!) > 4.0 && hasFinishedPath(entity, walkTarget, world.gameTime)) {
                lookTargetPos = walkTarget.target.currentBlockPosition()
                start(world, entity, l)
            }
        }
    }

    private fun withNodeFilter(
        entity: PathfinderMob,
        nodeFilter: Predicate<PathType>,
        block: () -> Unit) {
        val nodeEvaluator = entity.navigation.nodeEvaluator
        if (nodeEvaluator is OmniPathNodeMaker) {
            nodeEvaluator.nodeFilter = nodeFilter
            block()
            nodeEvaluator.nodeFilter = Predicate { true }
        } else {
            block()
        }
    }

    private fun hasFinishedPath(entity: PathfinderMob, walkTarget: WalkTarget, time: Long): Boolean {
        val blockPos = walkTarget.target.currentBlockPosition()
        this.path = generatePath(entity, walkTarget, blockPos)
        this.speed = walkTarget.speedModifier
        val brain = entity.brain
        if (hasReached(entity, walkTarget)) {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
        } else {
            val bl = this.path != null && path!!.canReach()
            if (bl) {
                brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
            } else if (!brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
                brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, time)
            }

            if (this.path != null) {
                return true
            }

            val vec3d = DefaultRandomPos.getPosTowards(
                entity,
                10,
                7,
                Vec3.atBottomCenterOf(blockPos),
                Math.PI / 2F
            )
            if (vec3d != null) {
                this.path = generatePath(entity, walkTarget, BlockPos.containing(vec3d))
                return this.path != null
            }
        }

        return false
    }

    private fun generatePath(
        entity: PathfinderMob,
        walkTarget: WalkTarget,
        target: BlockPos
    ): Path? {
        var path: Path? = null
        if (walkTarget is CobblemonWalkTarget) {
            withNodeFilter(
                entity = entity,
                nodeFilter = walkTarget.nodeTypeFilter,
            ) {
                var resolvedPath = entity.navigation.createPath(
                    walkTarget.target.currentBlockPosition(),
                    0
                )
                if (resolvedPath != null) {
                    // A CobblemonWalkTarget may have a destinationNodeTypeFilter, which is used to filter the end node of the path.
                    // Mainly useful for when the thing setting the target did it a bit crudely or didn't know how far the navigator
                    // was going to get, and might land on something that is technically traversable by the entity but it doesn't
                    // fit the intention of the task. Example, a little bird wandering might end up finishing on a flight node,
                    // but it doesn't really want to fly in place so it'd prefer not ending there.
                    while (resolvedPath.endNode?.type?.let { walkTarget.destinationNodeTypeFilter(it) } == false) {
                        resolvedPath.deleteNode(resolvedPath.nodeCount - 1)
                    }
                    if (resolvedPath.nodeCount == 0) {
                        resolvedPath = null
                    }
                }
                path = resolvedPath
            }
        } else {
            path = entity.navigation.createPath(target, 0)
        }
        return path
    }

    private fun hasReached(entity: PathfinderMob, walkTarget: WalkTarget): Boolean {
        return walkTarget.target.currentBlockPosition().distManhattan(entity.blockPosition()) <= walkTarget.closeEnoughDist
    }

    companion object {
        private fun isTargetSpectator(target: WalkTarget): Boolean {
            val lookTarget = target.target
            return if (lookTarget is EntityTracker) {
                lookTarget.entity.isSpectator
            } else {
                false
            }
        }
    }
}

