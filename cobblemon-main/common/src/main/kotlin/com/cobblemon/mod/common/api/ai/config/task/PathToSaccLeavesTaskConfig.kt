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
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.tasks.PathToSacLeafTask
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class PathToSaccLeavesTaskConfig : SingleTaskConfig {
    companion object {
        const val POLLINATE = "pollinate"
    }

    val condition = booleanVariable(POLLINATE, "can_pollinate", true).asExpressible()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(
        condition
    ).asVariables()

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (entity !is PokemonEntity) {
            return null
        }

        if (!checkCondition(behaviourConfigurationContext.runtime, condition)) {
            return null
        }
        behaviourConfigurationContext.addMemories(
CobblemonMemories.NEARBY_SACC_LEAVES,
            CobblemonMemories.HAS_NECTAR
        )
        behaviourConfigurationContext.addSensors(CobblemonSensors.NEARBY_SACC_LEAVES, CobblemonSensors.NEARBY_FLOWER)
        return PathToSacLeafTask.create()
    }
}