/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType

object StopTryingToReachItemTask {
    fun create(maxTimeToReachItem: Int, disableDuration: Int): BehaviorControl<LivingEntity?> {
        return BehaviorBuilder.create {
            it.group(
                it.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM),
                it.present(CobblemonMemories.TIME_TRYING_TO_REACH_WANTED_ITEM),
                it.registered(CobblemonMemories.DISABLE_WALK_TO_WANTED_ITEM)
            ).apply(it) { itemBlockPosMemory, timeTryingMemory, isDisabledMemory ->
                Trigger { world, entity, time ->
                    if (entity == null) return@Trigger false

                    val timeSpentTryingToMoveToWantedItem = it.get(timeTryingMemory)
                    if (timeSpentTryingToMoveToWantedItem > maxTimeToReachItem) {
                        itemBlockPosMemory.erase()
                        timeTryingMemory.erase()
                        isDisabledMemory.setWithExpiry(true, disableDuration.toLong())
                    } else {
                        timeTryingMemory.set(timeSpentTryingToMoveToWantedItem + 1)
                    }

                    return@Trigger true
                }
            }
        }
    }
}
