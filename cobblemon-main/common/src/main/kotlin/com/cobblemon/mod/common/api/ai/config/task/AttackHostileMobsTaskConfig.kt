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
import com.cobblemon.mod.common.entity.pokemon.ai.tasks.AttackHostileMobsTask
import com.mojang.serialization.Codec
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

class AttackHostileMobsTaskConfig : SingleTaskConfig {
    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): List<MoLangConfigVariable> {
        return emptyList()
    }

    override fun createTask(
        entity: LivingEntity,
        brainConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        return AttackHostileMobsTask.create()
    }
    
    companion object {
        val CODEC: Codec<AttackHostileMobsTaskConfig> = Codec.unit(AttackHostileMobsTaskConfig())
    }
}