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
import com.cobblemon.mod.common.api.ai.config.task.SharedEntityVariables.SEE_DISTANCE
import com.cobblemon.mod.common.util.resolveEntityTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType

class LookAtEntitiesTaskConfig : SingleTaskConfig {
    companion object {
        const val LOOK_AT_ENTITIES = "look_at_entities"
        const val LOOK_AT_ENTITY_TYPES = "look_at_entity_types"
    }

    val condition = booleanVariable(SharedEntityVariables.LOOKING_CATEGORY, LOOK_AT_ENTITIES, true).asExpressible()
    val maxDistance = numberVariable(SharedEntityVariables.LOOKING_CATEGORY, SEE_DISTANCE, 15).asExpressible()
    val entityTypes = stringVariable(SharedEntityVariables.LOOKING_CATEGORY, LOOK_AT_ENTITY_TYPES, "").asExpressible()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(
        condition,
        entityTypes,
        maxDistance
    ).asVariables()

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (!condition.resolveBoolean(behaviourConfigurationContext.runtime)) return null
        behaviourConfigurationContext.addMemories(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.LOOK_TARGET)
        behaviourConfigurationContext.addSensors(SensorType.NEAREST_LIVING_ENTITIES)

        val lookDistance = maxDistance.resolveFloat(behaviourConfigurationContext.runtime)
        val lookAtTypesText = entityTypes.resolveString(behaviourConfigurationContext.runtime)
        if (lookAtTypesText.isBlank()) {
            return SetEntityLookTarget.create(lookDistance)
        }

        val lookAtTypes = resolveEntityTypes(entity.registryAccess(), lookAtTypesText)
        return if (lookAtTypes.isNotEmpty()) {
            SetEntityLookTarget.create({ lookAtTypes.contains(it.type) }, lookDistance)
        } else {
            null
        }
    }
}