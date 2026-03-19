/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.mainThreadRuntime
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.resolveInt
import com.cobblemon.mod.common.util.withQueryValue
import kotlin.math.abs
import kotlin.math.ceil
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator

object MoveToOwnerTask {
    fun create(condition: Expression, completionRange: Expression, maxDistance: Expression, teleportDistance: Expression, speedMultiplier: Expression): OneShot<PokemonEntity> = BehaviorBuilder.create {
        it.group(
            it.registered(MemoryModuleType.WALK_TARGET)
        ).apply(it) { walkTarget ->
            Trigger { _, entity, _ ->
                val owner = entity.owner ?: return@Trigger false
                if (owner.level() != entity.level()) {
                    return@Trigger false
                }
                mainThreadRuntime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
                val condition = mainThreadRuntime.resolveBoolean(condition)
                if (!condition) {
                    return@Trigger false
                }

                val teleportDistance = mainThreadRuntime.resolveFloat(teleportDistance)
                val maxDistance = mainThreadRuntime.resolveFloat(maxDistance)
                val speedMultiplier = mainThreadRuntime.resolveFloat(speedMultiplier)
                val completionRange = mainThreadRuntime.resolveInt(completionRange)

                val horizontalHitboxRadius = maxOf(entity.boundingBox.xsize, entity.boundingBox.zsize) / 2

                if (entity.distanceTo(owner) - horizontalHitboxRadius > teleportDistance) {
                    if (tryTeleport(entity, owner)) {
                        entity.brain.eraseMemory(MemoryModuleType.LOOK_TARGET)
                        entity.brain.eraseMemory(MemoryModuleType.WALK_TARGET)
                    }
                    return@Trigger true
                } else if (entity.distanceTo(owner) - horizontalHitboxRadius > maxDistance && it.tryGet(walkTarget).isEmpty && !entity.brain.hasMemoryValue(CobblemonMemories.PATH_COOLDOWN)) {
                    entity.brain.setMemoryWithExpiry(CobblemonMemories.PATH_COOLDOWN, true, 60L)
                    entity.brain.setMemory(MemoryModuleType.LOOK_TARGET, EntityTracker(owner, true))
                    entity.brain.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(owner, speedMultiplier, maxOf(completionRange, ceil(horizontalHitboxRadius).toInt()) - 1))
                    return@Trigger true
                }
                return@Trigger false
            }
        }
    }

    private fun tryTeleport(entity: PokemonEntity, owner: Entity): Boolean {
        val blockPos = owner.blockPosition()
        for (i in 0..9) {
            val j = this.getRandomInt(entity.random, -3, 3)
            val k = this.getRandomInt(entity.random, -1, 1)
            val l = this.getRandomInt(entity.random, -3, 3)
            val succeeded = this.tryTeleportTo(entity, owner, blockPos.x + j, blockPos.y + k, blockPos.z + l)
            if (succeeded) {
                return true
            }
        }
        return false
    }

    private fun tryTeleportTo(entity: PokemonEntity, owner: Entity, x: Int, y: Int, z: Int): Boolean {
        if (abs(x.toDouble() - owner.x) < 2.0 && abs(z - owner.z) < 2.0) {
            return false
        } else if (!this.canTeleportTo(entity, BlockPos(x, y, z))) {
            return false
        } else {
            entity.moveTo(
                x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5,
                entity.yRot,
                entity.xRot
            )
            entity.navigation.stop()
            return true
        }
    }

    private fun canTeleportTo(entity: PokemonEntity, pos: BlockPos): Boolean {
        val pathNodeType = WalkNodeEvaluator.getPathTypeStatic(entity, pos.mutable())
        if (pathNodeType != PathType.WALKABLE) {
            return false // Could be more complex to support fliers and swimmers
        } else {
            val blockPos = pos.subtract(entity.blockPosition())
            return entity.level().noCollision(entity, entity.boundingBox.move(blockPos))
        }
    }

    private fun getRandomInt(random: RandomSource, min: Int, max: Int) = random.nextInt(max - min + 1) + min
}