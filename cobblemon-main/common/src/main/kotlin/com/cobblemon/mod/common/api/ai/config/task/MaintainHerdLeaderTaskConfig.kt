/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.asExpression
import com.mojang.datafixers.util.Either
import java.util.UUID
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType

/**
 * Task that looks around for a better herd leader or gives up on a leader
 * that is themselves following a different Pokémon.
 *
 * @author Hiroku
 * @since June 16th, 2025
 */
class MaintainHerdLeaderTaskConfig : SingleTaskConfig {
    // How frequently to check for a better herd leader
    val checkTicks: ExpressionOrEntityVariable = Either.left("60".asExpression())

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): List<MoLangConfigVariable> {
        return listOf(checkTicks).asVariables()
    }

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (entity !is PokemonEntity) {
            return null
        }
        behaviourConfigurationContext.addMemories(
            CobblemonMemories.HERD_LEADER,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        )
        behaviourConfigurationContext.addSensors(SensorType.NEAREST_LIVING_ENTITIES)
        val checkTicks = checkTicks.resolveInt(behaviourConfigurationContext.runtime)
        return BehaviorBuilder.create { instance ->
            instance.group(
                instance.present(CobblemonMemories.HERD_LEADER),
                instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            ).apply(instance) { herdLeader, nearbyEntities ->
                Trigger { world, entity, _ ->
                    entity as PokemonEntity
                    if (world.gameTime % checkTicks != 0L) {
                        return@Trigger false
                    }

                    val leader = instance.get(herdLeader).let(UUID::fromString).let(world::getEntity) as? PokemonEntity
                    val currentLeaderDefinition = leader?.let { leader ->
                        entity.behaviour.herd.toleratedLeaders.find { it.pokemon.matches(leader) }
                    }
                    if (leader == null || currentLeaderDefinition == null) {
                        entity.brain.eraseMemory(CobblemonMemories.HERD_LEADER)
                        return@Trigger true // No leader, so we erase the memory
                    } else {
                        val bestLeader = FindHerdLeaderTaskConfig.getBestHerdLeader(
                            currentLeader = (leader to currentLeaderDefinition),
                            nearbyEntities = instance.get(nearbyEntities),
                            entity = entity
                        )

                        if (bestLeader != null && bestLeader != leader) {
                            bestLeader.brain.eraseMemory(CobblemonMemories.HERD_LEADER)
                            entity.brain.setMemory(CobblemonMemories.HERD_LEADER, bestLeader.uuid.toString())
                            bestLeader.adjustHerdSize(1)
                            leader.adjustHerdSize(-1)
                        }
                    }
                    return@Trigger true
                }
            }
        }
    }
}