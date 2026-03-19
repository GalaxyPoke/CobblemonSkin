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

object StopTryingToReachSweetBerryBushTask {
    fun create(maxTimeToReachItem: Int, disableDuration: Int): BehaviorControl<LivingEntity?> {
        return BehaviorBuilder.create {
            it.group(
                it.present(CobblemonMemories.NEARBY_SWEET_BERRY_BUSH),
                it.present(CobblemonMemories.TIME_TRYING_TO_REACH_BERRY_BUSH),
                it.registered(CobblemonMemories.DISABLE_WALK_TO_BERRY_BUSH)
            ).apply(it) { blockPosMemory, timeTryingMemory, isDisabledMemory ->
                Trigger { world, entity, time ->
                    if (entity == null) return@Trigger false

                    val timeSpentTryingToMoveToBerryBush = entity.brain.getMemory(CobblemonMemories.TIME_TRYING_TO_REACH_BERRY_BUSH).orElse(0)

                    if (timeSpentTryingToMoveToBerryBush > maxTimeToReachItem) {
                        blockPosMemory.erase()
                        timeTryingMemory.erase()
                        isDisabledMemory.setWithExpiry(true, disableDuration.toLong())
                    } else {
                        timeTryingMemory.set(timeSpentTryingToMoveToBerryBush + 1)
                    }

                    return@Trigger true
                }
            }
        }
    }
}
