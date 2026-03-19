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
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.ai.ToleratedHerdLeader
import com.cobblemon.mod.common.util.asExpression
import com.mojang.datafixers.util.Either
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities
import net.minecraft.world.entity.ai.sensing.SensorType

class FindHerdLeaderTaskConfig : SingleTaskConfig {
    companion object {
        /**
         * Logic used to choose the best herd leader from nearby visible entities.
         *
         * - Higher tier leaders are always preferred.
         * - Same-tier leaders that are closer are preferred except for when they're the same tier as the current leader.
         * - We don't want to exceed the target's herd size limit.
         * - We don't want to choose a leader that is themselves following a leader.
         * - Some leader definitions allow lower-level Pokémon to lead them.
         * - Leaders just have to not be in a party - pastured and wild Pokémon can mingle.
         */
        fun getBestHerdLeader(
            currentLeader: Pair<PokemonEntity, ToleratedHerdLeader>?,
            nearbyEntities: NearestVisibleLivingEntities,
            entity: PokemonEntity
        ): PokemonEntity? {
            var bestLeader: PokemonEntity? = null
            val (currentLeaderEntity, currentLeaderDefinition) = currentLeader ?: Pair(null, null)
            var bestTier = currentLeaderDefinition?.tier?.let { it + 1 } ?: 0 // Only upgrade to a higher tier leader
            var bestDistance = Float.MAX_VALUE
            nearbyEntities.findAll {
                it is PokemonEntity &&
                        it != currentLeader &&
                        it.pokemon.storeCoordinates.get()?.store !is PartyStore &&
                        it.getHerdSize() < it.behaviour.herd.maxSize &&
                        !it.brain.hasMemoryValue(CobblemonMemories.POKEMON_DROWSY)
            }.forEach { possibleLeader ->
                possibleLeader as PokemonEntity
                val bestMatchingHerdLeader = entity.behaviour.herd.bestMatchLeader(entity, possibleLeader) ?: return@forEach
                val possibleLeaderIsAlreadyALeader = possibleLeader.brain.hasMemoryValue(CobblemonMemories.HERD_SIZE)
                val currentLeaderHerdTier = currentLeaderEntity?.getHerdTier() ?: entity.getHerdTier()
                if (currentLeaderHerdTier >= bestMatchingHerdLeader.tier) {
                    return@forEach // This leader is following a leader of a higher tier, we can't replace them
                }
                val distance = entity.distanceTo(possibleLeader)
                if (bestMatchingHerdLeader.tier < bestTier) {
                    return@forEach // No need to check further if this leader is worse than the best found so far
                } else if ((distance > bestDistance && !possibleLeaderIsAlreadyALeader) && bestMatchingHerdLeader.tier == bestTier) {
                    return@forEach // No need to check further if this leader is farther than the best found so far, unless they are already a leader - I'd prefer to join a pack
                } else {
                    bestLeader = possibleLeader
                    bestDistance = distance
                    bestTier = bestMatchingHerdLeader.tier
                }
            }
            return bestLeader ?: currentLeaderEntity
        }
    }

    // How frequently to check for whether it should herd. Probably isn't that expensive but might use this for chance.
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
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            CobblemonMemories.HERD_LEADER,
            MemoryModuleType.WALK_TARGET
        )
        behaviourConfigurationContext.addSensors(SensorType.NEAREST_LIVING_ENTITIES)
        val checkTicksValue = checkTicks.resolveInt(behaviourConfigurationContext.runtime)
        return BehaviorBuilder.create { instance ->
            instance.group(
                instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES),
                instance.absent(CobblemonMemories.HERD_LEADER)
            ).apply(instance) { nearbyEntities, herdLeader ->
                Trigger { world, entity, _ ->
                    if (world.gameTime % checkTicksValue != 0L) {
                        return@Trigger false
                    }

                    entity as PokemonEntity
                    val bestLeader = getBestHerdLeader(
                        currentLeader = null,
                        nearbyEntities = instance.get(nearbyEntities),
                        entity = entity
                    ) ?: return@Trigger false

                    herdLeader.set(bestLeader.uuid.toString())
                    bestLeader.brain.eraseMemory(CobblemonMemories.HERD_LEADER)
                    bestLeader.adjustHerdSize(1)
                    return@Trigger true
                }
            }
        }
    }
}