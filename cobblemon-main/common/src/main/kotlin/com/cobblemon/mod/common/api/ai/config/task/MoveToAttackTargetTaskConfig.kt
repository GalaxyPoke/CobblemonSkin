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
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.entity.ai.MoveToAttackTargetTask
import com.cobblemon.mod.common.util.asExpression
import com.mojang.datafixers.util.Either
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.memory.MemoryModuleType

class MoveToAttackTargetTaskConfig : SingleTaskConfig {
    val speedMultiplier = numberVariable(SharedEntityVariables.ATTACKING_CATEGORY, "attacking_movement_speed", 0.5).asExpressible()
    val closeEnoughDistance: ExpressionOrEntityVariable = Either.left("0".asExpression())

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(
        speedMultiplier,
        closeEnoughDistance
    ).asVariables()

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ) = MoveToAttackTargetTask.create(
        speedMultiplier = speedMultiplier.asExpression(),
        closeEnoughDistance = closeEnoughDistance.asExpression()
    ).also {
        behaviourConfigurationContext.addMemories(
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.WALK_TARGET
        )
    }
}