/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.*
import net.minecraft.world.entity.ai.behavior.EntityTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.ai.memory.WalkTarget


object MoveToItemTask {
    fun create(condition: Expression, maxDistance: Expression, speedMultiplier: Expression): OneShot<PokemonEntity> = BehaviorBuilder.create {
        it.group(
            it.absent(MemoryModuleType.WALK_TARGET),
            it.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM),
            it.absent(CobblemonMemories.DISABLE_WALK_TO_WANTED_ITEM),
            it.registered(CobblemonMemories.TIME_TRYING_TO_REACH_WANTED_ITEM)
        ).apply(it) { walkTarget, wantedItemEntity, isDisabledMemory, timeSpentReachingItem ->
            Trigger { _, entity, _ ->

                val itemEntity = it.get(wantedItemEntity)
                if (itemEntity == null || !itemEntity.isAlive || itemEntity.distanceTo(entity) > mainThreadRuntime.resolveFloat(maxDistance)) {
                    return@Trigger false
                }
                mainThreadRuntime.withQueryValue("entity", entity.asMostSpecificMoLangValue())

                val _condition = mainThreadRuntime.resolveBoolean(condition)
                if (!_condition || !entity.pokemon.canDropHeldItem) {
                    return@Trigger false // condition failed or Pokemon has been given an item to keep safe
                }
                val _speedMultiplier = mainThreadRuntime.resolveFloat(speedMultiplier)

                if (entity.brain.checkMemory(CobblemonMemories.TIME_TRYING_TO_REACH_WANTED_ITEM, MemoryStatus.VALUE_ABSENT)) {
                    entity.brain.setMemory(CobblemonMemories.TIME_TRYING_TO_REACH_WANTED_ITEM, 0)
                }
                entity.brain.setMemory(MemoryModuleType.LOOK_TARGET, EntityTracker(itemEntity, true))
                entity.brain.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(itemEntity, _speedMultiplier, 0))
                return@Trigger true
            }
        }
    }
}