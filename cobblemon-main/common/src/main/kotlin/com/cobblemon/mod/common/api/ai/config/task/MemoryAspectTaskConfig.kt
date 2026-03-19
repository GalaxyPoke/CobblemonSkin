/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.bedrockk.molang.ast.StringExpression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.getMemorySafely
import com.cobblemon.mod.common.util.resolveString
import com.mojang.datafixers.util.Either
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus

/**
 * A task that modifies the aspects of a Pokémon or NPC based on a memory module.
 * If the memory is registered, the aspect is added; if it is absent, the aspect is removed.
 *
 * @author Hiroku
 * @since July 6th, 2025
 */
class MemoryAspectTaskConfig : SingleTaskConfig {
    var memory: MemoryModuleType<*> = MemoryModuleType.DUMMY
    var aspect: ExpressionOrEntityVariable = Either.left(StringExpression(StringValue("")))

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(aspect).asVariables()
    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (entity !is PokemonEntity && entity !is NPCEntity) {
            return null
        } else if (memory == MemoryModuleType.DUMMY) {
            Cobblemon.LOGGER.error("MemoryAspectTaskConfig: Memory module type is not set for ${entity.name.string}.")
            return null
        } else if (aspect.left().orElse(null)?.originalString == "") {
            Cobblemon.LOGGER.error("MemoryAspectTaskConfig: Aspect is not set for ${entity.name.string}.")
            return null
        }

        val aspect = behaviourConfigurationContext.runtime.resolveString(aspect.asExpression())

        return object : Behavior<LivingEntity>(
            mapOf(memory to MemoryStatus.REGISTERED),
            Int.MAX_VALUE,
            Int.MAX_VALUE
        ) {
            override fun canStillUse(level: ServerLevel, entity: LivingEntity, gameTime: Long) = true

            override fun tick(level: ServerLevel, owner: LivingEntity, gameTime: Long) {
                val hasMemory = owner.brain.hasMemoryValue(memory)
                if (owner is PokemonEntity) {
                    if (!hasMemory && aspect in owner.pokemon.forcedAspects) {
                        owner.pokemon.forcedAspects -= aspect
                    } else if (hasMemory && aspect !in owner.pokemon.forcedAspects) {
                        owner.pokemon.forcedAspects += aspect
                    } else return
                    owner.pokemon.updateAspects()
                } else if (owner is NPCEntity) {
                    if (!hasMemory && aspect in owner.appliedAspects) {
                        owner.appliedAspects -= aspect
                    } else if (hasMemory && aspect !in owner.appliedAspects) {
                        owner.appliedAspects += aspect
                    } else return
                    owner.updateAspects()
                }
            }
        }
    }
}