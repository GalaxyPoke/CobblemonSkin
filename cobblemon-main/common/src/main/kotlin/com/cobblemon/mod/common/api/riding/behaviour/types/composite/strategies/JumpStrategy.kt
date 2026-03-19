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

object JumpStrategy : CompositeRidingStrategy<CompositeSettings> {

    override val key = cobblemonResource("strategy/jump")

    override fun tick(
        settings: CompositeSettings,
        state: CompositeState,
        defaultState: RidingBehaviourState,
        alternateState: RidingBehaviourState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ) {
        if (shouldTransitionToDefault(state, settings.defaultBehaviour, vehicle)) {
            transition(vehicle, settings, state, alternateState, defaultState, settings.defaultBehaviour)
        } else if (shouldTransitionToAlternative(state, settings.alternateBehaviour, vehicle, driver)) {
            transition(vehicle, settings, state, defaultState, alternateState, settings.alternateBehaviour)
        }
    }

    private fun shouldTransitionToDefault(
        state: CompositeState,
        defaultSettings: RidingBehaviourSettings,
        vehicle: PokemonEntity
    ): Boolean {
        if (state.activeBehaviour.get() == defaultSettings.key) return false
        if (!vehicle.onGround()) return false
        if (state.lastTransition.get() + 5 >= vehicle.level().gameTime) return false
        val defaultBehaviour = RidingBehaviours.get(defaultSettings.key)
        return defaultBehaviour.isActive(defaultSettings, state.defaultBehaviourState, vehicle)
    }

    private fun shouldTransitionToAlternative(
        state: CompositeState,
        alternativeSettings: RidingBehaviourSettings,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        if (state.activeBehaviour.get() == alternativeSettings.key) return false
        if (!driver.jumping) return false
        if (state.lastTransition.get() + 5 >= vehicle.level().gameTime) return false
        val alternativeBehaviour = RidingBehaviours.get(alternativeSettings.key)
        return alternativeBehaviour.isActive(alternativeSettings, state.alternateBehaviourState, vehicle)
    }

}
