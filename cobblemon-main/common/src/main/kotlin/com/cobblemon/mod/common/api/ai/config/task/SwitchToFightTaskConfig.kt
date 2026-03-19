/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.config.task

import com.cobblemon.mod.common.api.ai.BehaviourConfigurationContext
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.util.withQueryValue
import net.minecraft.world.Difficulty
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.schedule.Activity

class SwitchToFightTaskConfig : SingleTaskConfig {
    val activity = Activity.FIGHT

    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = emptyList<MoLangConfigVariable>()

    override fun createTask(
        entity: LivingEntity,
        behaviourConfigurationContext: BehaviourConfigurationContext
    ) = BehaviorBuilder.create {
        it.group(
            it.present(MemoryModuleType.ATTACK_TARGET)
        ).apply(it) { _ ->
            Trigger { world, entity, _ ->
                if (entity.commandSenderWorld.getCurrentDifficultyAt(entity.blockPosition()).difficulty == Difficulty.PEACEFUL) {
                    return@Trigger false
                }

                entity.brain.setActiveActivityIfPossible(activity)
                return@Trigger true
            }
        }
    }.also {
        behaviourConfigurationContext.addMemories(MemoryModuleType.ATTACK_TARGET)
    }
}