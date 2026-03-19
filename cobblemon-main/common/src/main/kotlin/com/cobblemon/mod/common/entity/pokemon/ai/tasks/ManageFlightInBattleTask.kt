/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.scheduling.afterOnServer
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder

import net.minecraft.world.entity.ai.behavior.declarative.Trigger

object ManageFlightInBattleTask {
    fun create(): OneShot<LivingEntity> = BehaviorBuilder.create { context ->
        context.group(
            context.present(CobblemonMemories.POKEMON_BATTLE)
        ).apply(context) {
            Trigger { _, entity, _ ->
                if (entity !is PokemonEntity) {
                    return@Trigger false
                }

                if (entity.exposedForm.behaviour.moving.fly.canFly) {
                    if (entity.ticksLived > 0 && !entity.getBehaviourFlag(PokemonBehaviourFlag.FLYING) && entity.navigation.isAirborne(entity.level(), entity.blockPosition())) {
                        // Let flyers fly in battle if they're in the air
                        entity.setBehaviourFlag(PokemonBehaviourFlag.FLYING, true)
                        afterOnServer(0.1F) {
                            entity.navigation.moveTo(entity.x, entity.y + 0.2, entity.z, 1.0)
                        }
                        return@Trigger true
                    }
                }
                return@Trigger false
            }
        }
    }
}
