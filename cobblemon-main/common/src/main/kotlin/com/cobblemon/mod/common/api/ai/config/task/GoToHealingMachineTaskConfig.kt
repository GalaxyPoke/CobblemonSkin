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
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.entity.npc.ai.GoToHealingMachineTask
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.withQueryValue
import com.mojang.datafixers.util.Either
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType

class GoToHealingMachineTaskConfig : SingleTaskConfig {
    val condition = booleanVariable(SELF_HEALING, USE_HEALING_MACHINES, true).asExpressible()
    val horizontalSearchRange: ExpressionOrEntityVariable = Either.left("10".asExpression())
    val verticalSearchRange: ExpressionOrEntityVariable = Either.left("5".asExpression())
    val completionRange: ExpressionOrEntityVariable = Either.left("1".asExpression())
    val walkSpeed = numberVariable(SharedEntityVariables.MOVEMENT_CATEGORY, SharedEntityVariables.WALK_SPEED, 0.35).asExpressible()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(condition, walkSpeed, horizontalSearchRange, verticalSearchRange, completionRange).asVariables()

    companion object {
        const val SELF_HEALING = "self_healing"
        const val USE_HEALING_MACHINES = "use_healing_machines"
    }

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (!condition.resolveBoolean(behaviourConfigurationContext.runtime)) return null
        behaviourConfigurationContext.addMemories(
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET,
            CobblemonMemories.NPC_BATTLING
        )
        behaviourConfigurationContext.addSensors(CobblemonSensors.NPC_BATTLING)
        return GoToHealingMachineTask.create(
            horizontalSearchRange = horizontalSearchRange.asExpression(),
            verticalSearchRange = verticalSearchRange.asExpression(),
            speedMultiplier = walkSpeed.asExpression(),
            completionRange = completionRange.asExpression()
        )
    }

}