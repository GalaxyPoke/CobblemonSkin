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
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.asExpression
import com.mojang.datafixers.util.Either
import kotlin.jvm.optionals.getOrNull
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType

/**
 * Checks nearby entities and counts how many have nominated this entity as their leader. We normally update this
 * as people join and leave the herd, but this is a fallback to make sure we don't get stuck with an incorrect value
 * like if the entities die or whatever.
 *
 * @author Hiroku
 * @since June 17th, 2025
 */
class CountFollowersTaskConfig : SingleTaskConfig {
    val checkTicks: ExpressionOrEntityVariable = Either.left("10".asExpression())

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): List<MoLangConfigVariable> {
        return listOf(checkTicks).asVariables()
    }

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        val checkTicksValue = checkTicks.resolveInt(behaviourConfigurationContext.runtime)
        behaviourConfigurationContext.addMemories(
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            CobblemonMemories.HERD_SIZE,
            CobblemonMemories.HERD_LEADER
        )
        behaviourConfigurationContext.addSensors(SensorType.NEAREST_LIVING_ENTITIES)
        return BehaviorBuilder.create { instance ->
            instance.group(
                instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES),
                instance.registered(CobblemonMemories.HERD_SIZE)
            ).apply(instance) { nearbyEntities, herdCount ->
                Trigger { world, entity, _ ->
                    if (world.gameTime % checkTicksValue != 0L) {
                        return@Trigger false
                    }
                    val myUUID = entity.uuid.toString()
                    val followersCount = instance.get(nearbyEntities).findAll {
                        it is PokemonEntity
                                && it.brain.hasMemoryValue(CobblemonMemories.HERD_LEADER)
                                && it.brain.getMemory(CobblemonMemories.HERD_LEADER).getOrNull() == myUUID
                    }.count()
                    if (followersCount == 0) {
                        herdCount.erase()
                    } else {
                        herdCount.set(followersCount)
                    }
                    return@Trigger true
                }
            }
        }
    }
}