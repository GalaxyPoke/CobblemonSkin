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
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.tasks.HarvestSweetBerryBushTask
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.memory.MemoryModuleType

class HarvestSweetBerryBushTaskConfig : SingleTaskConfig {
    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = emptyList<MoLangConfigVariable>()
    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (entity !is PokemonEntity) {
            return null
        }
        behaviourConfigurationContext.addMemories(
            CobblemonMemories.NEARBY_SWEET_BERRY_BUSH,
            CobblemonMemories.DISABLE_WALK_TO_BERRY_BUSH,
            CobblemonMemories.TIME_TRYING_TO_REACH_BERRY_BUSH,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET)
        behaviourConfigurationContext.addSensors(CobblemonSensors.NEARBY_SWEET_BERRY_BUSH)
        return HarvestSweetBerryBushTask()
    }
}