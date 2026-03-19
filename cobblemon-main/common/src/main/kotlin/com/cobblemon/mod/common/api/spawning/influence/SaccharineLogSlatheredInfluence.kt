/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.influence

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.fishing.FishingSpawnCause
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.client.effect.SaccharineLogBlockParticlesPacket
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import org.joml.Vector3d

class SaccharineLogSlatheredInfluence(val pos: BlockPos? = null) : SpawningInfluence {

    companion object {
        const val HONEY_DRENCHED_ASPECT = "honey_drenched"
        const val SACCHARINE_LOG_SLATHERED_MARKER = "saccharine_log_slathered"
        const val HIDDEN_ABILITY_CHANCE = 0.05
        const val SAFE_BLOCK_SEARCH_DISTANCE = 10
    }

    var activated = false

    override fun affectSpawnablePosition(spawnablePosition: SpawnablePosition) {
        spawnablePosition.markers.add(SACCHARINE_LOG_SLATHERED_MARKER)
    }

    override fun affectSpawn(action: SpawnAction<*>, entity: Entity) {
        if (entity is PokemonEntity) {
            // avoid double application or applying to a pokemon that already has a hidden ability
            if (entity.pokemon.forcedAspects.contains(HONEY_DRENCHED_ASPECT)
                || entity.pokemon.ability.priority !== Priority.LOWEST) return

            if (Math.random() <= HIDDEN_ABILITY_CHANCE) {
                FishingSpawnCause.alterHAAttempt(entity)
            }

            if (!activated) {
                val logPos = pos
                val level = action.spawnablePosition.world.level
                if (logPos != null) {
                    val blockState = level.getBlockState(logPos)
                    if (blockState.block == CobblemonBlocks.SACCHARINE_LOG_SLATHERED) {
                        val direction = blockState.getValue(HorizontalDirectionalBlock.FACING)
                        val safePos = attemptSafeMove(level, entity, logPos, direction)
                        if (safePos != null) entity.moveTo(safePos.toVec3d())

                        SaccharineLogBlockParticlesPacket(logPos, safePos).sendToPlayersAround(
                            logPos.x.toDouble(),
                            logPos.y.toDouble(),
                            logPos.z.toDouble(),
                            64.0,
                            level.dimension()
                        )

                        level.playSound(null, logPos, SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL)
                        val axis = blockState.getValue(RotatedPillarBlock.AXIS)
                        val newState = CobblemonBlocks.SACCHARINE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis)
                        level.setBlock(logPos, newState, 3)
                    }
                }
                entity.pokemon.forcedAspects += HONEY_DRENCHED_ASPECT
                activated = true
            }
        }
    }

    fun attemptSafeMove(level: ServerLevel, entity: PokemonEntity, startPos: BlockPos, direction: Direction): BlockPos? {
        val inWater = entity.isInWater
        val originalPos = startPos.relative(direction)
        val maxRadius = SAFE_BLOCK_SEARCH_DISTANCE

        // For each radius from 0 up to max, increase radius after each full downward traversal
        for (radius in 0..maxRadius) {
            var currentPos = originalPos
            repeat(SAFE_BLOCK_SEARCH_DISTANCE) {
                val safeSpot = searchInHalfCircle(level, entity, currentPos, direction, radius, inWater)
                if (safeSpot != null) {
                    return safeSpot
                }
                currentPos = currentPos.below()
            }
        }
        return null
    }

    fun searchInHalfCircle(level: ServerLevel, entity: PokemonEntity, centerPos: BlockPos, direction: Direction, searchRadius: Int, allowWater: Boolean): BlockPos? {
        for (dx in -searchRadius..searchRadius) {
            for (dz in -searchRadius..searchRadius) {
                if (dx * dx + dz * dz <= searchRadius * searchRadius) {
                    val candidatePos = centerPos.offset(dx, 0, dz)
                    val candidateVec = Vector3d(dx.toDouble(), 0.0, dz.toDouble())
                    val directionVec = Vector3d(direction.stepX.toDouble(), direction.stepY.toDouble(), direction.stepZ.toDouble())

                    // Check if position is in front of the direction
                    val dot = candidateVec.dot(directionVec)
                    if (dot >= 0) {
                        // Check collision at candidate position
                        val currentBox = entity.boundingBox.move(
                            candidatePos.x - entity.x,
                            candidatePos.y - entity.y,
                            candidatePos.z - entity.z
                        )
                        if (level.noCollision(entity, currentBox)) {
                            // Check block below
                            val blockBelowPos = candidatePos.below()
                            val blockState = level.getBlockState(blockBelowPos)

                            val isWater = blockState.block == Blocks.WATER

                            if (allowWater) {
                                if (isWater) return candidatePos
                            } else {
                                if (!blockState.isAir) return candidatePos
                            }
                        }
                    }
                }
            }
        }
        return null
    }
}
