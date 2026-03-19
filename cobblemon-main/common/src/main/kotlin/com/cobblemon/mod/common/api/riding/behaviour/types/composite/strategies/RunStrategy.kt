/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour.types.composite.strategies

import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourState
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviours
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeState
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

object RunStrategy : CompositeRidingStrategy<CompositeSettings> {

    override val key = cobblemonResource("strategy/run")

    override fun tick(
        settings: CompositeSettings,
        state: CompositeState,
        defaultState: RidingBehaviourState,
        alternateState: RidingBehaviourState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ) {
        if (shouldTransitionToDefault(state, settings.defaultBehaviour, vehicle, driver)) {
            transition(vehicle, settings, state, alternateState, defaultState, settings.defaultBehaviour)
        }
        else if (shouldTransitionToAlternate(state, settings.alternateBehaviour, vehicle, driver, input)) {
            transition(vehicle, settings, state, defaultState, alternateState, settings.alternateBehaviour)
        }
    }

    private fun shouldTransitionToDefault(
        state: CompositeState,
        defaultSettings: RidingBehaviourSettings,
        entity: PokemonEntity,
        driver: Player
    ): Boolean {
        if (state.activeBehaviour.get() == defaultSettings.key) return false
        if (!entity.onGround()) return false
        if (state.lastTransition.get() + 20 >= entity.level().gameTime) return false
        val defaultBehaviour = RidingBehaviours.get(defaultSettings.key)
        val ret = defaultBehaviour.isActive(defaultSettings, state.defaultBehaviourState, entity)
        return ret
    }

    private fun shouldTransitionToAlternate(
        state: CompositeState,
        alternativeSettings: RidingBehaviourSettings,
        entity: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Boolean {
        if (state.activeBehaviour.get() == alternativeSettings.key) return false
        if (state.lastTransition.get() + 20 >= entity.level().gameTime) return false
        val alternativeBehaviour = RidingBehaviours.get(alternativeSettings.key)
        return driver.isSprinting && input.z > 0.5 && alternativeBehaviour.isActive(alternativeSettings, state.alternateBehaviourState, entity)
    }

}
