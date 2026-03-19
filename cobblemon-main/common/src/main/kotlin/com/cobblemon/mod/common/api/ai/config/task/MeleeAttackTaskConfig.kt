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
import com.cobblemon.mod.common.entity.npc.ai.MeleeAttackTask
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.memory.MemoryModuleType

class MeleeAttackTaskConfig : SingleTaskConfig {
    val condition = booleanVariable(SharedEntityVariables.ATTACKING_CATEGORY, "attacks_melee", true).asExpressible()
    val range = numberVariable(SharedEntityVariables.ATTACKING_CATEGORY, "melee_range", 0.75F).asExpressible()
    val cooldownTicks = numberVariable(SharedEntityVariables.ATTACKING_CATEGORY, "melee_cooldown", 20).asExpressible()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(
        condition,
        range,
        cooldownTicks
    ).asVariables()

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (!condition.resolveBoolean(behaviourConfigurationContext.runtime)) return null
        behaviourConfigurationContext.addMemories(
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN
        )
        return MeleeAttackTask.create(
            range = range.asExpression(),
            cooldownTicks = cooldownTicks.asExpression()
        )
    }
}