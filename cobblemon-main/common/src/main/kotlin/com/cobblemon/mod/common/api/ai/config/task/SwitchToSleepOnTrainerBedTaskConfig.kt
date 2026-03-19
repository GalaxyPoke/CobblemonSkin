/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.CobblemonActivities
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.level.block.BedBlock

class SwitchToSleepOnTrainerBedTaskConfig : SingleTaskConfig {
    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = emptyList<MoLangConfigVariable>()
    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (entity !is PokemonEntity) {
            return null
        }

        if (entity.pokemon.isWild()) {
            return null
        }

        behaviourConfigurationContext.addMemories(
            CobblemonMemories.POKEMON_SLEEPING,
            CobblemonMemories.POKEMON_BATTLE,
            MemoryModuleType.WALK_TARGET
        )

        return BehaviorBuilder.create {
            it.group(
                it.absent(CobblemonMemories.POKEMON_BATTLE),
                it.absent(CobblemonMemories.POKEMON_SLEEPING),
                it.registered(MemoryModuleType.WALK_TARGET)
            ).apply(it) { _, _, walkTarget ->
                Trigger { world, entity, _ ->
                    entity as PokemonEntity
                    val owner = entity.owner ?: return@Trigger false
                    if (!owner.isSleeping || owner.distanceTo(entity) > 12) {
                        return@Trigger false
                    }

                    val blockPos = owner.blockPosition()
                    val blockState = entity.level().getBlockState(blockPos)
                    val bedPos = blockState.getOptionalValue(BedBlock.FACING).orElse(null)
                        ?.let { direction -> blockPos.relative(direction.opposite) }
                        ?: BlockPos(blockPos)

                    walkTarget.set(WalkTarget(bedPos.center, 0.35F, 0))
                    entity.brain.setActiveActivityIfPossible(CobblemonActivities.POKEMON_SLEEP_ON_TRAINER_BED)
                    return@Trigger true
                }
            }
        }
    }
}