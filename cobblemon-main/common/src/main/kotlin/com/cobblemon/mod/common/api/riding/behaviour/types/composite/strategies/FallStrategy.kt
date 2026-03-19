/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour.types.composite.strategies

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourState
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviours
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeState
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

object FallStrategy : CompositeRidingStrategy<FallCompositeSettings> {

    override val key = cobblemonResource("strategy/fall")

    override fun tick(
        settings: FallCompositeSettings,
        state: CompositeState,
        defaultState: RidingBehaviourState,
        alternateState: RidingBehaviourState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ) {
        if (shouldTransitionToDefault(state, settings.defaultBehaviour, vehicle)) {
            transition(vehicle, settings, state, alternateState, defaultState, settings.defaultBehaviour)
        } else if (shouldTransitionToAlternate(settings, state, settings.alternateBehaviour, vehicle, driver)) {
            transition(vehicle, settings, state, defaultState, alternateState, settings.alternateBehaviour)
        }
    }

    private fun shouldTransitionToDefault(
        state: CompositeState,
        defaultSettings: RidingBehaviourSettings,
        vehicle: PokemonEntity
    ): Boolean {
        if (state.activeBehaviour.get() == defaultSettings.key) return false
        if (state.lastTransition.get() + 20 >= vehicle.level().gameTime) return false
        if (!vehicle.onGround()) return false
        val defaultBehaviour = RidingBehaviours.get(defaultSettings.key)
        return defaultBehaviour.isActive(defaultSettings, state.defaultBehaviourState, vehicle)
    }

    private fun shouldTransitionToAlternate(
        settings: FallCompositeSettings,
        state: CompositeState,
        alternateSettings: RidingBehaviourSettings,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        if (state.activeBehaviour.get() == alternateSettings.key) return false
        if (!driver.jumping) return false
        if (state.lastTransition.get() + 20 >= vehicle.level().gameTime) return false

        val runtime = vehicle.runtime
        val minFallingSpeed = runtime.resolveFloat(settings.minimumFallSpeed)
        val minForwardSpeed = runtime.resolveFloat(settings.minimumForwardSpeed)
        if (vehicle.deltaMovement.y < -minFallingSpeed) return false
        if (vehicle.deltaMovement.horizontalDistance() < minForwardSpeed) return false
        val alternativeBehaviour = RidingBehaviours.get(alternateSettings.key)
        return alternativeBehaviour.isActive(alternateSettings, state.alternateBehaviourState, vehicle)
    }

}

class FallCompositeSettings : CompositeSettings() {
    override val key = FallStrategy.key

    var minimumForwardSpeed: Expression = "0.0".asExpression()
        private set

    var minimumFallSpeed: Expression = "0.5".asExpression()
        private set

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeExpression(minimumForwardSpeed)
        buffer.writeExpression(minimumFallSpeed)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        super.decode(buffer)
        minimumForwardSpeed = buffer.readExpression()
        minimumFallSpeed = buffer.readExpression()
    }
}
