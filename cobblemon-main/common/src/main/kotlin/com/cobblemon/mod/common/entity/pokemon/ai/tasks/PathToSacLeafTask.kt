/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.WalkTarget

object PathToSacLeafTask {


    fun create(): OneShot<in LivingEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.registered(MemoryModuleType.LOOK_TARGET),
                it.absent(MemoryModuleType.WALK_TARGET),
                it.present(CobblemonMemories.HAS_NECTAR),
                it.present(CobblemonMemories.NEARBY_SACC_LEAVES)
            ).apply(it) { lookTarget, walkTarget, pollinated, saccLeavesPos ->
                Trigger { world, entity, time ->
                    if (entity !is PathfinderMob || !entity.isAlive || entity !is PokemonEntity || !it.get(pollinated) || world.isRaining || world.isNight) {
                        return@Trigger false
                    }
                    val targetPos = it.get(saccLeavesPos)
                    walkTarget.set(WalkTarget(targetPos, 0.35F, 0))
                    lookTarget.set(BlockPosTracker(targetPos.toVec3d().add(0.0, entity.eyeHeight.toDouble(), 0.0)))

                    return@Trigger true
                }
            }
        }
    }
}