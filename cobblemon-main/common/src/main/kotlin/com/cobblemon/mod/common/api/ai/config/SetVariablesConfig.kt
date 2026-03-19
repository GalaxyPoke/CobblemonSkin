/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.entity.MoLangScriptingEntity
import com.cobblemon.mod.common.util.resolve
import net.minecraft.world.entity.LivingEntity

class SetVariablesConfig : BehaviourConfig {
    var variableValues = mutableMapOf<String, ExpressionLike>()

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = emptyList<MoLangConfigVariable>()
    override fun configure(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) {
        if (entity is MoLangScriptingEntity) {
            variableValues.forEach { (variableName, valueExpression) ->
                entity.config.setDirectly(variableName, behaviourConfigurationContext.runtime.resolve(valueExpression))
            }
        }
    }
}