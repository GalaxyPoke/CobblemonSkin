/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.DoNothing

interface SingleTaskConfig : TaskConfig {
    companion object {
        fun nothing() = object : SingleTaskConfig {
            override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = emptyList<MoLangConfigVariable>()
            override fun createTask(
                entity: LivingEntity,
                behaviourConfigurationContext: BehaviourConfigurationContext
            ): BehaviorControl<in LivingEntity>? {
                return DoNothing(0, 1)
            }
        }
    }

    override fun createTasks(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): List<BehaviorControl<in LivingEntity>> {
        return createTask(entity, behaviourConfigurationContext)?.let { listOf(it) } ?: emptyList()
    }

    fun createTask(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): BehaviorControl<in LivingEntity>?
}