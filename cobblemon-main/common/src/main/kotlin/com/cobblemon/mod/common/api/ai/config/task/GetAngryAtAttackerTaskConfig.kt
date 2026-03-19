/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.api.ai.config.task.SharedEntityVariables.ATTACKING_CATEGORY
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.entity.ai.GetAngryAtAttackerTask
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType

class GetAngryAtAttackerTaskConfig : SingleTaskConfig {
    companion object {
        const val GET_ANGRY_AT_ATTACKER = "get_angry_at_attacker"
    }

    val condition = booleanVariable(ATTACKING_CATEGORY, GET_ANGRY_AT_ATTACKER, true).asExpressible()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(condition).asVariables()

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (!condition.resolveBoolean(behaviourConfigurationContext.runtime)) return null
        behaviourConfigurationContext.addMemories(
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.ANGRY_AT,
        )
        behaviourConfigurationContext.addSensors(SensorType.HURT_BY)
        return GetAngryAtAttackerTask.create()
    }
}