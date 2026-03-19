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
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.toDF
import com.cobblemon.mod.common.util.withQueryValue
import com.mojang.datafixers.util.Either
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.RunOne

/**
 * A task where each time the brain is ticked, a different task might run. This differs from [RandomTaskConfig] in that
 * the same entity can run all of the different possibilities, rather than [RandomTaskConfig] where once the entity
 * spawns, it will only use whichever was selected at the time of spawning.
 *
 * This is most useful for making a single entity randomly choose what to do at a given moment.
 *
 * @author Hiroku
 * @since October 19th, 2024
 */
class OneOfTaskConfig : SingleTaskConfig {
    class OneOfTaskOption {
        val weight: Int = 1
        val task: TaskConfig = SingleTaskConfig.nothing()
    }

    val condition: ExpressionOrEntityVariable = Either.left("true".asExpression())
    val options = mutableListOf<OneOfTaskOption>()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = options.flatMap { it.task.getVariables(entity, behaviourConfigurationContext) }

    override fun createTask(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): BehaviorControl<in LivingEntity>? {
        if (!condition.resolveBoolean(behaviourConfigurationContext.runtime)) return null
        return RunOne(options.map { it.task.createTasks(entity, behaviourConfigurationContext).first() toDF it.weight })
    }
}