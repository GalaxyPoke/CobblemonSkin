/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.monster.Slime
import net.minecraft.world.entity.monster.piglin.AbstractPiglin

object AttackHostileMobsTask {
    fun create(): OneShot<LivingEntity> = BehaviorBuilder.create {
        it.group(
            it.registered(MemoryModuleType.ATTACK_TARGET),
            it.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
        ).apply(it) { attackTarget, nearestVisibleLiving ->
            Trigger { world, entity, _ ->
                val currentTarget = entity.brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null)

                // If we have a current target, check if it's still valid and within roam range (if tethered)
                if (currentTarget != null && currentTarget.isAlive) {
                    val tethering = (entity as? PokemonEntity)?.tethering
                    if (tethering == null || tethering.canRoamTo(currentTarget.blockPosition())) {
                        return@Trigger false // Keep the target
                    } else {
                        // Target moved out of range let's kill the memory >:)
                        entity.brain.eraseMemory(MemoryModuleType.ATTACK_TARGET)
                    }
                }

                // Target is invalid or gone let's find a new one
                entity.brain.eraseMemory(MemoryModuleType.ATTACK_TARGET)

                val nearby = (entity.brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(null)
                    ?: return@Trigger false).findAll { true}

                // Filter for hostile, non-player, living mobs around the pasture radius if pastured. todo maybe we want to add Pokemon too?
                val hostile = nearby.firstOrNull { potentialTarget ->
                    if (potentialTarget !is Mob || potentialTarget is Slime || potentialTarget is AbstractPiglin || potentialTarget !is Enemy || !potentialTarget.isAlive || potentialTarget is Creeper)
                        return@firstOrNull false

                    val tethering = (entity as? PokemonEntity)?.tethering
                    return@firstOrNull if (tethering != null) {
                        tethering.canRoamTo(potentialTarget.blockPosition())
                    } else {
                        true
                    }
                }

                if (hostile != null) {
                    entity.brain.setMemory(MemoryModuleType.ATTACK_TARGET, hostile)
                    true
                } else {
                    false
                }
            }
        }
    }
}
