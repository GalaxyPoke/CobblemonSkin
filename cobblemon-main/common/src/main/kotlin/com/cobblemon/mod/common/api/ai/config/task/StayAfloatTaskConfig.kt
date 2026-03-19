/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.WrapperLivingEntityTask
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.entity.ai.StayAfloatTask
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class StayAfloatTaskConfig : SingleTaskConfig {
    val condition = booleanVariable(SharedEntityVariables.MOVEMENT_CATEGORY, "can_float", true).asExpressible()
    val chance = numberVariable(SharedEntityVariables.MOVEMENT_CATEGORY, "float_chance", 0.8F).asExpressible()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(condition, chance).asVariables()
    override fun createTask(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): BehaviorControl<LivingEntity>? {
        if (!condition.resolveBoolean(behaviourConfigurationContext.runtime)) return null
        behaviourConfigurationContext.addMemories(CobblemonMemories.POKEMON_BATTLE)
        return WrapperLivingEntityTask(
            StayAfloatTask(chance.resolveFloat(behaviourConfigurationContext.runtime)),
            PathfinderMob::class.java
        )
    }
}