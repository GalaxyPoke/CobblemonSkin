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

class SwitchToPanicWhenHurtTaskConfig : SingleTaskConfig {
    var condition = booleanVariable(SharedEntityVariables.FEAR_CATEGORY, "panic_when_hurt", true).asExpressible()
    var includePassiveDamage =  booleanVariable(SharedEntityVariables.FEAR_CATEGORY, "panic_on_passive_damage", false).asExpressible()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(
        condition,
        includePassiveDamage
    ).asVariables()

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (!condition.resolveBoolean(behaviourConfigurationContext.runtime)) return null
        return if (includePassiveDamage.resolveBoolean(behaviourConfigurationContext.runtime)) {
            behaviourConfigurationContext.addMemories(MemoryModuleType.HURT_BY)
            behaviourConfigurationContext.addSensors(SensorType.HURT_BY)
            BehaviorBuilder.create {
                it.group(it.present(MemoryModuleType.HURT_BY))
                    .apply(it) { _ ->
                        Trigger { world, entity, _ ->
                            (entity as? PathfinderMob)?.navigation?.stop()
                            entity.brain.setActiveActivityIfPossible(Activity.PANIC)
                            return@Trigger true
                        }
                    }
            }
        } else {
            behaviourConfigurationContext.addMemories(MemoryModuleType.HURT_BY_ENTITY)
            behaviourConfigurationContext.addSensors(SensorType.HURT_BY)
            BehaviorBuilder.create {
                it.group(it.present(MemoryModuleType.HURT_BY_ENTITY))
                    .apply(it) { _ ->
                        Trigger { world, entity, _ ->
                            (entity as? PathfinderMob)?.navigation?.stop()
                            entity.brain.setActiveActivityIfPossible(Activity.PANIC)
                            return@Trigger true
                        }
                    }
            }
        }
    }
}