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
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType
import net.minecraft.world.entity.schedule.Activity

class SwitchToPanicWhenHostilesNearbyTaskConfig : SingleTaskConfig {
    var condition = booleanVariable(SharedEntityVariables.FEAR_CATEGORY, "panic_when_hostiles_nearby", true).asExpressible()
    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(condition).asVariables()

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (!condition.resolveBoolean(behaviourConfigurationContext.runtime)) return null
        behaviourConfigurationContext.addMemories(MemoryModuleType.NEAREST_HOSTILE)
        behaviourConfigurationContext.addSensors(SensorType.VILLAGER_HOSTILES)
        return BehaviorBuilder.create {
            it.group(it.present(MemoryModuleType.NEAREST_HOSTILE))
                .apply(it) { nearestHostile ->
                    Trigger { world, entity, _ ->
                        (entity as? PathfinderMob)?.navigation?.stop()
                        entity.brain.setActiveActivityIfPossible(Activity.PANIC)
                        return@Trigger true
                    }
                }
        }
    }
}