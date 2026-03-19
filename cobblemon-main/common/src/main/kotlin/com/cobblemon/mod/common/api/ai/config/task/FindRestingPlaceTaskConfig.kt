/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.CobblemonSensors
import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.ai.ExpressionOrEntityVariable
import com.cobblemon.mod.common.api.ai.WrapperLivingEntityTask
import com.cobblemon.mod.common.api.ai.asVariables
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.entity.pokemon.ai.tasks.FindRestingPlaceTask
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolveBoolean
import com.mojang.datafixers.util.Either
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.memory.MemoryModuleType

class FindRestingPlaceTaskConfig : SingleTaskConfig {
    val condition: ExpressionLike = "true".asExpressionLike()
    val horizontalSearchDistance: ExpressionOrEntityVariable = Either.left("16".asExpression())
    val verticalSearchDistance: ExpressionOrEntityVariable = Either.left("5".asExpression())

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = listOf(horizontalSearchDistance, verticalSearchDistance).asVariables()
    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ): BehaviorControl<in LivingEntity>? {
        if (entity !is PokemonEntity) {
            return null
        }
        if (!behaviourConfigurationContext.runtime.resolveBoolean(condition)) {
            return null
        }

        behaviourConfigurationContext.addMemories(
            CobblemonMemories.PATH_COOLDOWN,
            CobblemonMemories.POKEMON_DROWSY,
            MemoryModuleType.WALK_TARGET
        )
        behaviourConfigurationContext.addSensors(CobblemonSensors.POKEMON_DROWSY)

        return WrapperLivingEntityTask(
            FindRestingPlaceTask.create(horizontalSearchDistance.resolveInt(behaviourConfigurationContext.runtime), verticalSearchDistance.resolveInt(behaviourConfigurationContext.runtime)),
            PokemonEntity::class.java
        )
    }
}