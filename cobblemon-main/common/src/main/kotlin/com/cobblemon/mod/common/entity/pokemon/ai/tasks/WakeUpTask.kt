/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.world.entity.ai.behavior.OneShot
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder
import net.minecraft.world.entity.ai.behavior.declarative.Trigger

object WakeUpTask {
    fun create(): OneShot<PokemonEntity> {
        return BehaviorBuilder.create {
            it.group(
                it.absent(CobblemonMemories.POKEMON_DROWSY)
            ).apply(it) { _ ->
                Trigger { _, entity, _ ->
                    val hasSleepStatus = entity.pokemon.status?.status == Statuses.SLEEP
                    if (hasSleepStatus && entity.pokemon.storeCoordinates.get()?.store is PartyStore) {
                        return@Trigger false // You can't wake up if you're in the party with the sleep status.
                    }
                    if (hasSleepStatus) {
                        entity.pokemon.status = null
                    }
                    entity.brain.eraseMemory(CobblemonMemories.POKEMON_SLEEPING)
                    entity.brain.useDefaultActivity()
                    return@Trigger true
                }
            }
        }
    }
}