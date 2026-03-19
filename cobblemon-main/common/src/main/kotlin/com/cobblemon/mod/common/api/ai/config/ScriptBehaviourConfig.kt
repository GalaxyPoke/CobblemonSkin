/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.withQueryValue
import com.mojang.datafixers.util.Either
import net.minecraft.world.entity.LivingEntity

/**
 * Runs a MoLang script as part of the entity's behaviour configuration. Variables can be configured here for convenience.
 *
 * When [executeEarly] is true, the script will run in an earlier phase than most configs, allowing it to get ahead of
 * other behaviour configurations that might depend on the changes made by the script.
 *
 * @author Hiroku
 * @since August 11th, 2025
 */
class ScriptBehaviourConfig : BehaviourConfig {
    /** If true, the script will execute as part of the getVariables phase. */
    val executeEarly: Boolean = false
    val condition: ExpressionOrEntityVariable = Either.left("true".asExpression())
    val script: ExpressionLike = "0".asExpressionLike()
    val variables = mutableListOf<MoLangConfigVariable>()

    private fun runScript(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) {
        if (!checkCondition(behaviourConfigurationContext, condition)) return
        script.resolve(behaviourConfigurationContext.runtime)
    }

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = variables

    override fun preconfigure(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) {
        if (executeEarly) {
            runScript(entity = entity, behaviourConfigurationContext = behaviourConfigurationContext)
        }
    }

    override fun configure(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) {
        if (!executeEarly) {
            runScript(entity = entity, behaviourConfigurationContext = behaviourConfigurationContext)
        }
    }
}