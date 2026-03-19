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
import net.minecraft.core.Direction
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.phys.Vec3

class PointToSpawnTaskConfig : SingleTaskConfig {
    override fun getVariables(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext) = emptyList<MoLangConfigVariable>()
    override fun createTask(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): BehaviorControl<in LivingEntity>? {
        behaviourConfigurationContext.addMemories(MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET)
        return BehaviorBuilder.create {
            it.group(
                it.registered(MemoryModuleType.LOOK_TARGET),
                it.absent(MemoryModuleType.WALK_TARGET)
            ).apply(it) { lookTargetMemory, _ ->
                Trigger { world, entity, _ ->
                    val lookTarget = entity.brain.getMemory(MemoryModuleType.LOOK_TARGET).orElse(null)
                    if (lookTarget == null) {
                        lookTargetMemory.set(BlockPosTracker(Vec3.atCenterOf(entity.level().sharedSpawnPos).with(Direction.Axis.Y, entity.eyeY)))
                        return@Trigger true
                    } else {
                        return@Trigger false
                    }
                }
            }
        }
    }
}