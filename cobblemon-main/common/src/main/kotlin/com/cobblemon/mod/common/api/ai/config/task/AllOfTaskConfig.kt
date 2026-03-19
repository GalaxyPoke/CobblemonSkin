/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.util.asExpression
import com.mojang.datafixers.util.Either
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class AllOfTaskConfig : TaskConfig {
    val condition: ExpressionOrEntityVariable = Either.left("true".asExpression())
    val tasks: List<TaskConfig> = emptyList()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = tasks.flatMap { it.getVariables(entity, behaviourConfigurationContext) }
    override fun createTasks(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): List<BehaviorControl<in LivingEntity>> {
        if (!condition.resolveBoolean(behaviourConfigurationContext.runtime)) return emptyList()
        return tasks.flatMap { it.createTasks(entity, behaviourConfigurationContext) }
    }
}