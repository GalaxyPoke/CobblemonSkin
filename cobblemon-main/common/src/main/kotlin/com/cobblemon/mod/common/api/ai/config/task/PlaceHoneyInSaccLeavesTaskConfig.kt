/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.CobblemonActivities
import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.block.SaccharineLeafBlock
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.getMemorySafely
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.phys.Vec3

class PlaceHoneyInSaccLeavesTaskConfig : SingleTaskConfig {
    companion object {
        const val POLLINATE = "pollinate"
        const val MIN_DURATION : Int = 300
        const val MAX_DURATION : Int = 400
        const val REQUIRED_SUCCESSFUL_POLLINATION_TICKS = 250
    }

    val condition = booleanVariable(POLLINATE, "can_pollinate", true).asExpressible()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(
        condition
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

        return object : Behavior<LivingEntity>(
            mapOf(
                CobblemonMemories.NEARBY_SACC_LEAVES to MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET to MemoryStatus.VALUE_ABSENT,
                CobblemonMemories.HAS_NECTAR to MemoryStatus.VALUE_PRESENT,
            ),
            MIN_DURATION,
            MAX_DURATION
        ) {
            var successfulPollinationTicks = 0
            var lastSoundPlayedTick = 0
            var hoverPos: Vec3? = null

            override fun checkExtraStartConditions(level: ServerLevel, owner: LivingEntity): Boolean {
                if (level.isNight || level.isRaining || !(entity.brain.getMemorySafely(CobblemonMemories.HAS_NECTAR).orElse(false))) return false
                val optionalBlockPos = entity.brain.getMemorySafely(CobblemonMemories.NEARBY_SACC_LEAVES)
                if (!optionalBlockPos.isPresent || level.getBlockState(optionalBlockPos.get()).block != CobblemonBlocks.SACCHARINE_LEAVES) return false
                return Vec3.atCenterOf(optionalBlockPos.get()).distanceTo(entity.position()) <= 0.6
            }

            override fun canStillUse(level: ServerLevel, entity: LivingEntity, gameTime: Long): Boolean {
                return checkExtraStartConditions(level, entity) && entity.brain.activeActivities.indexOf(
                    CobblemonActivities.POKEMON_POLLINATION) != -1
            }

            override fun start(level: ServerLevel, entity: LivingEntity, gameTime: Long) {
                super.start(level, entity, gameTime)
                hoverPos = null
            }

            override fun tick(level: ServerLevel, owner: LivingEntity, gameTime: Long) {
                ++successfulPollinationTicks
                if (entity.random.nextFloat() < 0.05f && successfulPollinationTicks > this.lastSoundPlayedTick + 60) {
                    this.lastSoundPlayedTick = successfulPollinationTicks
                    entity.playSound(SoundEvents.BEEHIVE_DRIP, 1.0f, 1.0f)
                }
                val leavesPos = entity.brain.getMemorySafely(CobblemonMemories.NEARBY_SACC_LEAVES).orElse(null)

                val bl = hoverPos == null || entity.position().distanceTo(hoverPos!!) <= 0.1
                val bl3 = entity.random.nextInt(40) == 0
                if (bl && bl3) {
                    // mimics the wriggling over a flower the vanilla bees do
                    if (hoverPos == null) {
                        hoverPos = leavesPos.bottomCenter.add(0.0, 0.4, 0.0)
                    }
                    if (leavesPos != null) {
                        hoverPos = leavesPos.bottomCenter.add(Vec3((entity.random.nextDouble() * 2.0 - 1.0) * (1.0/5.0), 0.3 + (entity.random.nextDouble() * 2.0 - 1.0) * (1.0/7.0), (entity.random.nextDouble() * 2.0 - 1.0) * (1.0/5.0)))
                        entity.getMoveControl()
                            .setWantedPosition(hoverPos!!.x(), hoverPos!!.y(), hoverPos!!.z(), 0.35)
                        entity.lookControl.setLookAt(leavesPos.bottomCenter.add(0.0,0.5,0.0))
                    }
                } else if (!bl) {
                    entity.getMoveControl()
                        .setWantedPosition(hoverPos!!.x(), hoverPos!!.y(), hoverPos!!.z(), 0.35)
                }
            }

            override fun stop(level: ServerLevel, entity: LivingEntity, gameTime: Long) {
                if (successfulPollinationTicks > REQUIRED_SUCCESSFUL_POLLINATION_TICKS) {
                    val blockPos = entity.brain.getMemorySafely(CobblemonMemories.NEARBY_SACC_LEAVES).orElse(null)
                    blockPos?.let {
                        val blockState = level.getBlockState(blockPos)
                        if (blockState.block == CobblemonBlocks.SACCHARINE_LEAVES) {
                            val saccAge = blockState.getValue( SaccharineLeafBlock.AGE)
                            if (saccAge < SaccharineLeafBlock.MAX_AGE) {
                                entity.brain.eraseMemory(CobblemonMemories.HAS_NECTAR)
                                val newBlockState = blockState.setValue(SaccharineLeafBlock.AGE,  saccAge + 1)
                                level.setBlock(blockPos, newBlockState, 3)
                            }
                        }
                    }
                }
                successfulPollinationTicks = 0
                entity.brain.eraseMemory(MemoryModuleType.WALK_TARGET)
            }
        }
    }
}