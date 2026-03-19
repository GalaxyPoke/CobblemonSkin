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
import com.cobblemon.mod.common.api.ai.WrapperLivingEntityTask
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.tasks.EatGrassTask
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.memory.MemoryModuleType

class EatGrassTaskConfig : SingleTaskConfig {
    companion object {
        const val EATING_GRASS = "eating_grass"
        const val COOLDOWN = "eating_grass_cooldown_ticks"
        const val EAT_GRASS_CHANCE = "eat_grass_chance"
    }

    val cooldown = numberVariable(category = EATING_GRASS, name = COOLDOWN, default = 20 * 20).asExpressible()
    val eatGrassChance = numberVariable(category = EATING_GRASS, name = EAT_GRASS_CHANCE, default = 1 / (20 * 20F)).asExpressible()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): List<MoLangConfigVariable> {
        return listOf(
            cooldown,
            eatGrassChance
        ).asVariables()
    }

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (entity !is PokemonEntity) {
            return null
        }

        val chance = eatGrassChance.resolveFloat(behaviourConfigurationContext.runtime)
        if (chance == 0F) {
            return null
        }

        behaviourConfigurationContext.addMemories(MemoryModuleType.WALK_TARGET, CobblemonMemories.RECENTLY_ATE_GRASS)

        return WrapperLivingEntityTask(
            EatGrassTask(
                chance,
                cooldown.resolveInt(behaviourConfigurationContext.runtime).toLong()
            ),
            PokemonEntity::class.java
        )
    }
}