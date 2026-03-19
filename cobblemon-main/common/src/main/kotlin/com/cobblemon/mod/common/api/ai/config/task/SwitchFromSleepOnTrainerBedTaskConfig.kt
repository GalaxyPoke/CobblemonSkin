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
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger

class SwitchFromSleepOnTrainerBedTaskConfig : SingleTaskConfig {
    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = emptyList<MoLangConfigVariable>()
    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        // Why'd you give them this task then, dipshit?
        if (entity !is PokemonEntity) {
            return null
        }

        behaviourConfigurationContext.addMemories(CobblemonMemories.POKEMON_SLEEPING)
        return BehaviorBuilder.create {
            it.group(
                it.registered(CobblemonMemories.POKEMON_SLEEPING)
            ).apply(it) { pokemonSleeping ->
                Trigger { world, entity, _ ->
                    entity as PokemonEntity

                    // If they're owner is gone then we can consider that a reason to wake up, dogs do this too
                    val owner = entity.owner

                    val isCurrentlySleeping = entity.brain.getMemory(CobblemonMemories.POKEMON_SLEEPING).orElse(false)
                    // If we should move back to regular behaviours...
                    if (owner == null || !owner.isSleeping) {
                        // If it's willingly sleeping then it can lose the sleeping memory
                        if (isCurrentlySleeping && entity.pokemon.status?.status != Statuses.SLEEP) {
                            pokemonSleeping.erase()
                            return@Trigger false
                        }

                        // Go back to your usual behaviours
                        entity.brain.useDefaultActivity()
                        return@Trigger false
                    }

                    return@Trigger true
                }
            }
        }
    }

}