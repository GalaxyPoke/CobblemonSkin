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
import com.cobblemon.mod.common.api.ai.config.task.SharedEntityVariables.LOOKING_CATEGORY
import com.cobblemon.mod.common.entity.ai.LookInDirectionTask
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.memory.MemoryModuleType

class LookInDirectionTaskConfig : SingleTaskConfig {
    var condition = booleanVariable(LOOKING_CATEGORY, "locked_rotation", true).asExpressible()
    var yaw = numberVariable(LOOKING_CATEGORY, "locked_yaw", 0F).asExpressible()
    var pitch = numberVariable(LOOKING_CATEGORY, "locked_pitch", 0F).asExpressible()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(
        condition,
        yaw,
        pitch
    ).asVariables()

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ) = LookInDirectionTask(
        shouldLock = condition.asExpression(),
        yaw = yaw.asExpression(),
        pitch = pitch.asExpression()
    ).also {
        behaviourConfigurationContext.addMemories(MemoryModuleType.LOOK_TARGET)
    }
}