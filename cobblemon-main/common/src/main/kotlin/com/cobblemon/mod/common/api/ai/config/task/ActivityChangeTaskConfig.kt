/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.mainThreadRuntime
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.schedule.Activity

class ActivityChangeTaskConfig : SingleTaskConfig {
    val activity: Activity = Activity.IDLE
    val condition: ExpressionLike = "true".asExpressionLike()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): List<MoLangConfigVariable> = emptyList()

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity> {
        return object : OneShot<LivingEntity>() {
            override fun trigger(serverLevel: ServerLevel, livingEntity: LivingEntity, l: Long): Boolean {
                mainThreadRuntime.withQueryValue("entity", livingEntity.asMostSpecificMoLangValue())
                if (condition.resolveBoolean(mainThreadRuntime)) {
                    livingEntity.brain.setActiveActivityIfPossible(activity)
                    return true
                } else {
                    return false
                }
            }
        }
    }
}