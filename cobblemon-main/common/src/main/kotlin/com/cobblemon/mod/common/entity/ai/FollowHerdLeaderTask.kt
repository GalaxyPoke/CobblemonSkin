/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.ai

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.PokemonMoveControl
import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer
import com.cobblemon.mod.common.util.getMemorySafely
import com.cobblemon.mod.common.util.toBlockPos
import com.google.common.collect.ImmutableMap
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class FollowHerdLeaderTask : Behavior<PokemonEntity>(
    ImmutableMap.of(
        CobblemonMemories.HERD_LEADER, MemoryStatus.VALUE_PRESENT,
        MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
    ),
    Int.MAX_VALUE,
    Int.MAX_VALUE
) {
    var leader: PokemonEntity? = null
    var tooFar = 8F
    var closeEnough = 4F
    /** Chance per tick of following the herd leader. */
    val chance = 1 / 60F

    override fun checkExtraStartConditions(level: ServerLevel, owner: PokemonEntity): Boolean {
        leader = level.getEntity(owner.brain.getMemory(CobblemonMemories.HERD_LEADER).map(UUID::fromString).orElse(null) ?: return false) as? PokemonEntity
        val definition = leader?.let { leader -> owner.behaviour.herd.bestMatchLeader(owner, leader) }
        definition?.let {
            tooFar = (it.followDistance?.endInclusive ?: owner.behaviour.herd.followDistance.endInclusive).toFloat()
            closeEnough = (it.followDistance?.start ?: owner.behaviour.herd.followDistance.start).toFloat()
        }
        return leader != null
    }

    override fun canStillUse(level: ServerLevel, entity: PokemonEntity, gameTime: Long) = leader?.isAlive == true && leader?.uuid?.toString() == entity.brain.getMemory(CobblemonMemories.HERD_LEADER).orElse(null)

    override fun tick(level: ServerLevel, entity: PokemonEntity, gameTime: Long) {
        val leader = leader ?: return

        if (entity.brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
            return // We're busy goin' n' shit
        }

        val moveControl = entity.moveControl as? PokemonMoveControl ?: return
        val leaderMoveControl = leader.moveControl as? PokemonMoveControl ?: return

        if (leader.distanceTo(entity) > tooFar) {
            removeSleep(entity)
            entity.brain.setMemory(
                MemoryModuleType.WALK_TARGET,
                CobblemonWalkTarget(
                    pos = leader.position().add(getRandomOffset(entity, level)).toBlockPos(),
                    speedModifier = 0.4F,
                    completionRange = closeEnough.toInt()
                )
            )
        } else if (leader.isFlying() && !entity.isFlying()) {
            if (!entity.canFly()) {
                entity.brain.eraseMemory(CobblemonMemories.HERD_LEADER) // We can't go where this guy's going
                return
            }

            removeSleep(entity)
            entity.brain.setMemory(
                MemoryModuleType.WALK_TARGET,
                CobblemonWalkTarget(
                    pos = leader.position().add(getRandomOffset(entity, level)).toBlockPos(),
                    speedModifier = 0.3F,
                    completionRange = closeEnough.toInt()
                )
            )
        } else if (leaderMoveControl.banking) {
            removeSleep(entity)
            if (leader.isInLiquid && !entity.isInLiquid) {
                // We should get into water asap so we can mimic the banking
                entity.brain.setMemory(
                    MemoryModuleType.WALK_TARGET,
                    CobblemonWalkTarget(
                        pos = leader.position().add(getRandomOffset(entity, level)).toBlockPos(),
                        speedModifier = 0.4F,
                        completionRange = 1
                    )
                )
            } else {
                entity.yRot = leader.yRot
                moveControl.startBanking(
                    forwardBlocksPerTick = leaderMoveControl.bankForwardBlocksPerTick,
                    upwardsBlocksPerTick = leaderMoveControl.bankUpwardsBlocksPerTick,
                    durationTicks = 2,
                    rightDegreesPerTick = 0F // Relying on the above yRot = leader.yRot because we dunno when we first started mimicking the banking
                )
            }
        } else if (leader.brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
            if (entity.random.nextFloat() > chance) {
                return // Not this tick
            }

            removeSleep(entity)

            // Go to the leader's walk target, roughly.
            val walkTarget = leader.brain.getMemory(MemoryModuleType.WALK_TARGET).orElse(null) ?: return

            // We're already closer to the walk target than the leader is, so no need to move, if anything we should get out of the way
            if (entity.distanceToSqr(walkTarget.target.currentPosition()) + closeEnough < leader.distanceToSqr(walkTarget.target.currentPosition())) {
                return
            }

            entity.brain.setMemory(
                MemoryModuleType.WALK_TARGET,
                CobblemonWalkTarget(
                    pos = walkTarget.target.currentPosition().add(getRandomOffset(entity, level)).toBlockPos(),
                    speedModifier = walkTarget.speedModifier,
                    nodeTypeFilter = (walkTarget as? CobblemonWalkTarget)?.nodeTypeFilter ?: { true },
                    completionRange = closeEnough.toInt()
                )
            )
        } else if (leader.getCurrentPoseType() == PoseType.SLEEP) {
            // Leader is sleeping, if I'm drowsy and can sleep here then I will sleep too.
            if (entity.canSleepAt(entity.blockPosition().offset(0, -1, 0)) && entity.brain.getMemorySafely(CobblemonMemories.POKEMON_DROWSY).orElse(false)) {
                if (entity.random.nextFloat() < 1 / 40F) {
                    applySleep(entity)
                }
            }
        } else if (entity.brain.getMemorySafely(CobblemonMemories.POKEMON_SLEEPING).orElse(false)) {
            // Leader is awake, so we should be awake too.
            if (entity.random.nextFloat() < 1 / 15F) {
                removeSleep(entity)
            }
        }
    }

    fun getRandomOffset(entity: PokemonEntity, level: ServerLevel): Vec3 {
        val leader = leader ?: return Vec3.ZERO

        val minRange = closeEnough.toDouble()
        val maxRange = (tooFar - closeEnough / 2).toDouble()
        val randomDistance = Mth.nextDouble(level.random, minRange, maxRange)
        val randomAngle = Mth.nextDouble(level.random, 0.0, 2 * Math.PI)

        var offset = Vec3(cos(randomAngle) * randomDistance, 0.0, sin(randomAngle) * randomDistance)
        
        if ((entity.isInLiquid && leader.isInLiquid) || (entity.isFlying() && leader.isFlying())) {
            val verticalRange = randomDistance * 0.5
            val randomY = Mth.nextDouble(level.random, -verticalRange, verticalRange)
            offset = offset.add(0.0, randomY, 0.0)
        }

        return offset
    }

    fun applySleep(entity: PokemonEntity) {
        entity.brain.setMemory(CobblemonMemories.POKEMON_SLEEPING, true)
        entity.pokemon.status = PersistentStatusContainer(Statuses.SLEEP)
    }

    fun removeSleep(entity: PokemonEntity) {
        if (entity.brain.hasMemoryValue(CobblemonMemories.POKEMON_SLEEPING)) {
            entity.brain.eraseMemory(CobblemonMemories.POKEMON_SLEEPING)
            val status = entity.pokemon.status
            if (status != null && status.status == Statuses.SLEEP) {
                entity.pokemon.status = null
            }
        }
    }
}