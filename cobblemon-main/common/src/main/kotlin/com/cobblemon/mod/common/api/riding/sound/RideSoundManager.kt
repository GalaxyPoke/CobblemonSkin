/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.sound

import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.CompositeState
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation

/**
 * Class to control playing all ride sounds
 *
 * @author Jackowes
 * @since April 26th, 2025
 */
class RideSoundManager(
    private val ride: PokemonEntity
) {
    private val activeSounds = mutableListOf<RideLoopSound>()
    private var rideActive: Boolean = false
    private var currKey: ResourceLocation = cobblemonResource("no_key")

    fun tick() {
        val currActive = ride.hasControllingPassenger()

        if( !currActive && !this.rideActive) return

        // Remove stopped sounds
        activeSounds.removeIf { it.isStopped}

        if (!currActive && this.rideActive) {
            this.rideActive = false
            this.currKey = cobblemonResource("no_key")
            this.stop()
        } else if (currActive && !this.rideActive) {
            this.rideActive = true
            this.currKey = getActiveRideKey()
            this.getBehaviourSounds()
        } else if (currActive && this.rideActive && this.currKey != getActiveRideKey()) {
            this.currKey = getActiveRideKey()
            this.stop()
            this.getBehaviourSounds()
        }
    }

    fun getBehaviourSounds() {
        val rideSounds = ride.ifRidingAvailableSupply(null) { behaviour, settings, state ->
            behaviour.getRideSounds(settings, state, ride)
        }
        rideSounds?.sounds?.forEach {
            val loopingRideSound = RideLoopSound(ride,it)
            activeSounds.add(loopingRideSound)
            Minecraft.getInstance().soundManager.play(loopingRideSound)
        }
    }

    fun getActiveRideKey(): ResourceLocation {
        val key = ride.ridingController?.context?.settings?.key ?: cobblemonResource("no_key")
        if (key == CompositeBehaviour.KEY) {
            return (ride.ridingController?.context?.state as? CompositeState)?.activeBehaviour?.get() ?: cobblemonResource("no_key")
        } else {
            return key
        }
    }

    fun stop() {
        activeSounds.forEach { it.stopSound() }
    }
}