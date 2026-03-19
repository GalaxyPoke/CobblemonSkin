/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.ai

import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.PokemonMoveControl
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus

/**
 * Task that causes the entity to casually strafe around at a random turn angle for a little while. Think of fish
 * swimming in circles or birds circling around in the sky.
 *
 * @author Hiroku
 * @since June 6th, 2025
 */
class CircleAroundTask(
    val poseTypes: Set<PoseType>,
    val minTurnAngleDegrees: Float,
    val maxTurnAngleDegrees: Float,
    val verticalSpeed: Float,
    val speed: Float,
    minDurationTicks: Int = 60,
    maxDurationTicks: Int = 180
) : Behavior<PokemonEntity>(
    mapOf(
        MemoryModuleType.WALK_TARGET to MemoryStatus.VALUE_ABSENT,
        MemoryModuleType.PATH to MemoryStatus.VALUE_ABSENT
    ), minDurationTicks, maxDurationTicks
) {
    var chosenTurnAngle = 0F

    override fun checkExtraStartConditions(level: ServerLevel, entity: PokemonEntity): Boolean {
        return entity.getCurrentPoseType() in poseTypes
    }

    override fun canStillUse(level: ServerLevel, entity: PokemonEntity, l: Long): Boolean {
        return entity.getCurrentPoseType() in poseTypes
                && entity.brain.getMemory(MemoryModuleType.WALK_TARGET).isEmpty
                && entity.brain.getMemory(MemoryModuleType.PATH).isEmpty
    }

    override fun stop(world: ServerLevel, entity: PokemonEntity, l: Long) {
        (entity.moveControl as? PokemonMoveControl)?.stopBanking()
    }

    override fun start(arg: ServerLevel, entity: PokemonEntity, time: Long) {
        chosenTurnAngle = entity.random.nextFloat() * (maxTurnAngleDegrees - minTurnAngleDegrees) + minTurnAngleDegrees
        if (entity.random.nextBoolean()) {
            chosenTurnAngle = -chosenTurnAngle
        }
    }

    override fun tick(world: ServerLevel, entity: PokemonEntity, l: Long) {
        (entity.moveControl as? PokemonMoveControl)?.startBanking(
            forwardBlocksPerTick = speed,
            upwardsBlocksPerTick = verticalSpeed,
            rightDegreesPerTick = chosenTurnAngle,
            durationTicks = 2
        )
    }
}