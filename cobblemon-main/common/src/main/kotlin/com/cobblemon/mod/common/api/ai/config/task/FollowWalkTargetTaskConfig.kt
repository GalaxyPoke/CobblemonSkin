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
import com.cobblemon.mod.common.api.ai.WrapperLivingEntityTask
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.entity.ai.FollowWalkTargetTask
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.withQueryValue
import com.mojang.datafixers.util.Either
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.memory.MemoryModuleType

class FollowWalkTargetTaskConfig : SingleTaskConfig {
    val condition: ExpressionOrEntityVariable = Either.left("true".asExpression())
    val minRunTicks: ExpressionOrEntityVariable = Either.left("150".asExpression())
    val maxRunTicks: ExpressionOrEntityVariable = Either.left("250".asExpression())

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(minRunTicks, maxRunTicks).asVariables()

    override fun createTask(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): BehaviorControl<LivingEntity>? {
        behaviourConfigurationContext.addMemories(MemoryModuleType.WALK_TARGET, MemoryModuleType.PATH, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
        return WrapperLivingEntityTask(
            FollowWalkTargetTask(minRunTicks.resolveInt(behaviourConfigurationContext.runtime), maxRunTicks.resolveInt(behaviourConfigurationContext.runtime)),
            PathfinderMob::class.java
        )
    }
}