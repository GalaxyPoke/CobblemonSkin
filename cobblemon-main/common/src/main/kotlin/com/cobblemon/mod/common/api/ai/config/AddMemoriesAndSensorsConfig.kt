/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.resolveBoolean
import com.mojang.datafixers.util.Either
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType

/**
 * Does nothing except conditionally add some memories and sensors to the brain of the entity.
 *
 * @author Hiroku
 * @since June 27th, 2025
 */
class AddMemoriesAndSensorsConfig : BehaviourConfig {
    val condition: ExpressionOrEntityVariable = Either.left("true".asExpression())
    val memories: Set<MemoryModuleType<*>> = emptySet()
    val sensors: Set<SensorType<*>> = mutableSetOf()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(condition).asVariables()
    override fun configure(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) {
        if (!checkCondition(behaviourConfigurationContext, condition)) return
        behaviourConfigurationContext.addMemories(memories)
        behaviourConfigurationContext.addSensors(sensors)
    }
}