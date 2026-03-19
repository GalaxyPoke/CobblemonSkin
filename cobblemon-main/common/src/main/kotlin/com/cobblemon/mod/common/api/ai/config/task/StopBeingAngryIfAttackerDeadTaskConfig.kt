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
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead
import net.minecraft.world.entity.ai.memory.MemoryModuleType

class StopBeingAngryIfAttackerDeadTaskConfig : SingleTaskConfig {
    val condition = booleanVariable(SharedEntityVariables.ATTACKING_CATEGORY, "stop_being_angry_if_attacker_dead", true).asExpressible()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(condition).asVariables()
    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (!condition.resolveBoolean(behaviourConfigurationContext.runtime)) return null
        behaviourConfigurationContext.addMemories(MemoryModuleType.ANGRY_AT)
        return StopBeingAngryIfTargetDead.create()
    }
}