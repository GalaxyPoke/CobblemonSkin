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
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.TamableAnimal
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

class SleepIfOnTrainerBedTaskConfig : SingleTaskConfig {
    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): List<MoLangConfigVariable>  = emptyList()
    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity> {
        behaviourConfigurationContext.addMemories(
            MemoryModuleType.WALK_TARGET,
            CobblemonMemories.POKEMON_SLEEPING
        )
        return BehaviorBuilder.create {
            it.group(
                it.registered(MemoryModuleType.WALK_TARGET),
                it.absent(CobblemonMemories.POKEMON_SLEEPING)
            ).apply(it) { walkTarget, pokemonSleeping ->
                Trigger { world, entity, _ ->
                    if (entity !is TamableAnimal) {
                        return@Trigger false
                    }

                    val owner = entity.owner ?: return@Trigger false
                    // If it's no longer moving and is generally within range, cancel any outstanding movement targeting and sleep.
                    if (!entity.moveControl.hasWanted() && owner.distanceTo(entity) < 1.5 && owner.isSleeping && entity.blockStateOn.`is`(BlockTags.BEDS)) {
                        pokemonSleeping.set(true)
                        walkTarget.erase()
                        entity.onPathfindingDone()
                        return@Trigger true
                    }

                    return@Trigger false
                }
            }
        }
    }
}