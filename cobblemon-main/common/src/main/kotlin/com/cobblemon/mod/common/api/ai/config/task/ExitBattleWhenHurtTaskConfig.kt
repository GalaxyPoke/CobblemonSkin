/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.CobblemonSensors
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.battles.BattleRegistry
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType

class ExitBattleWhenHurtTaskConfig : SingleTaskConfig {
    var condition = booleanVariable(SharedEntityVariables.BATTLING_CATEGORY, "exit_battle_when_hurt", true).asExpressible()
    var includePassiveDamage = booleanVariable(SharedEntityVariables.BATTLING_CATEGORY, "exit_battle_from_passive_damage", true).asExpressible()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(
        condition,
        includePassiveDamage
    ).asVariables()

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (!condition.resolveBoolean(behaviourConfigurationContext.runtime)) return null

        val includePassiveDamage = includePassiveDamage.resolveBoolean(behaviourConfigurationContext.runtime)

        behaviourConfigurationContext.addMemories(
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY
        )
        behaviourConfigurationContext.addSensors(SensorType.HURT_BY)

        if (entity is NPCEntity) {
            behaviourConfigurationContext.addMemories(CobblemonMemories.NPC_BATTLING)
            behaviourConfigurationContext.addSensors(CobblemonSensors.NPC_BATTLING)
            fun cancelNPCBattles(npcEntity: NPCEntity): Boolean {
                val battles = npcEntity.battleIds.mapNotNull(BattleRegistry::getBattle)
                battles.forEach { it.end() }
                return battles.isNotEmpty()
            }

            return if (includePassiveDamage) {
                 BehaviorBuilder.create {
                    it.group(it.present(MemoryModuleType.HURT_BY), it.present(CobblemonMemories.NPC_BATTLING))
                        .apply(it) { _, _ -> Trigger { world, entity, _ -> return@Trigger cancelNPCBattles(entity as NPCEntity) } }
                }
            } else {
                BehaviorBuilder.create {
                    it.group(it.present(MemoryModuleType.HURT_BY_ENTITY), it.present(CobblemonMemories.NPC_BATTLING))
                        .apply(it) { _, _ -> Trigger { world, entity, _ -> return@Trigger cancelNPCBattles(entity as NPCEntity) } }
                }
            }
        } else if (entity is PokemonEntity) {
            behaviourConfigurationContext.addMemories(CobblemonMemories.POKEMON_BATTLE)
            fun cancelPokemonBattle(pokemonEntity: PokemonEntity): Boolean {
                val battle = BattleRegistry.getBattle(pokemonEntity.battleId ?: return false)
                battle?.end()
                return battle != null
            }

            return if (includePassiveDamage) {
                BehaviorBuilder.create {
                    it.group(it.present(MemoryModuleType.HURT_BY), it.present(CobblemonMemories.POKEMON_BATTLE))
                        .apply(it) { _, _ -> Trigger { world, entity, _ -> return@Trigger cancelPokemonBattle(entity as PokemonEntity) } }
                }
            } else {
                BehaviorBuilder.create {
                    it.group(it.present(MemoryModuleType.HURT_BY_ENTITY), it.present(CobblemonMemories.POKEMON_BATTLE))
                        .apply(it) { _, _ -> Trigger { world, entity, _ -> return@Trigger cancelPokemonBattle(entity as PokemonEntity) } }
                }
            }
        } else {
            return null
        }
    }
}