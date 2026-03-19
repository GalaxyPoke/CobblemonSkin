/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.DoubleJump
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.client.MountedCameraTypeHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.FluidTags
import net.minecraft.world.entity.LivingEntity
import kotlin.collections.get
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import kotlin.collections.get
import kotlin.math.max

/**
 * Small wrapper around a RidingBehaviour to provide sane defaults in the event that the behaviour is not active.
 *
 * @author landonjw
 */
class RidingController(
    val entity: PokemonEntity,
    val behaviours: Map<RidingStyle, RidingBehaviourSettings>) {

    var lastTransitionAge: Int = 0
        private set

    var context: ActiveRidingContext? = null
        private set

    fun changeBehaviour(behaviour: ResourceLocation) {
        if (entity.form.riding.behaviours == null) return
        val style = entity.form.riding.behaviours!!.filter { it.value.key == behaviour }.keys.firstOrNull() ?: return
        val behaviourSettings = entity.pokemon.riding.behaviours?.get(style) ?: return
        val newState = RidingBehaviours.get(behaviourSettings.key).createDefaultState(behaviourSettings)
        context?.let { newState.stamina.set(it.state.stamina.get(), forced = true) }
        context = ActiveRidingContext(behaviourSettings.key, behaviourSettings, newState, style)
        lastTransitionAge = entity.ticksLived
    }

    fun tick() {
        if (entity.ticksLived - lastTransitionAge < 10) return
        val newTransition = checkForNewTransition(entity.controllingPassenger)
        // If transitioning to air then give an upward boost
        if (newTransition == RidingStyle.AIR && context?.style != RidingStyle.AIR && context?.state != null) {
            val currVel = context!!.state.rideVelocity.get()
            context!!.state.rideVelocity.set(Vec3(currVel.x, max(0.1, currVel.y), currVel.z))
        }
        val behaviourSettings = entity.pokemon.riding.behaviours?.get(newTransition)
        if (newTransition != context?.style) {
            if (newTransition != null && behaviourSettings != null) {
                val newState = RidingBehaviours.get(behaviourSettings.key).createDefaultState(behaviourSettings)
                context?.let { newState.stamina.set(it.state.stamina.get(), forced = true) }
                context?.let { newState.rideVelocity.set(it.state.rideVelocity.get(), forced = true) }
                val previousKey = context?.behaviour
                val newKey = behaviourSettings.key
                for (passenger in entity.passengers) {
                    if (passenger is Player && passenger !is ServerPlayer) {
                        MountedCameraTypeHandler.handleTransition(passenger, previousKey ?: continue, newKey)
                    }
                }

                context = ActiveRidingContext(behaviourSettings.key, behaviourSettings, newState, newTransition)
                lastTransitionAge = entity.ticksLived
            } else {
                context = null
            }
        }

        if (context == null && !this.entity.level().isClientSide) {
            entity.ejectPassengers()
        }
    }

    fun getBehaviour(): RidingBehaviour<RidingBehaviourSettings, RidingBehaviourState>? {
        if (context == null) return null
        return RidingBehaviours.get(context!!.behaviour)
    }

    private fun checkForNewTransition(driver: LivingEntity?): RidingStyle? {
        when (context?.style) {
            RidingStyle.AIR -> {
                if (canTransitionToLand()) return RidingStyle.LAND
                if (canTransitionToLiquid()) return RidingStyle.LIQUID
                return RidingStyle.AIR
            }

            RidingStyle.LIQUID -> {
                if (canTransitionToAir(driver)) return RidingStyle.AIR
                if (canTransitionToLand()) return RidingStyle.LAND
                return RidingStyle.LIQUID
            }

            RidingStyle.LAND -> {
                if (canTransitionToAir(driver)) return RidingStyle.AIR
                if (canTransitionToLiquid()) return RidingStyle.LIQUID
                return RidingStyle.LAND
            }

            null -> {
                if (canTransitionToAir(driver)) return RidingStyle.AIR
                if (canTransitionToLiquid()) return RidingStyle.LIQUID
                if (canTransitionToLand()) return RidingStyle.LAND
                return null
            }
        }
    }

    private fun canTransitionToLand(): Boolean {
        if ((entity.isUnderWater)) return false
        if ((entity.isEyeInFluid(FluidTags.WATER) || entity.isEyeInFluid(FluidTags.LAVA))) return false
        // If the currently in the air but has no land style then don't transition and render the ride useless.
        // Hover is a good example for this.
        val shouldStayInAir = entity.pokemon.riding.behaviours?.get(RidingStyle.LAND) == null && context?.style == RidingStyle.AIR
        return entity.onGround() && !shouldStayInAir
    }

    private fun canTransitionToLiquid(): Boolean {
        // If it has a liquid controller to transition to and it isn't currently an air controller then transition on
        // touching water. If not then check for eyes in water. This allows fliers to fly closely above water and
        // for walkers that don't have a liquid controller to be able to walk in not deep water
        return if (entity.pokemon.riding.behaviours?.get(RidingStyle.LIQUID) != null && context?.style != RidingStyle.AIR) {
            entity.isInLiquid || entity.isUnderWater
        } else {
            entity.isEyeInFluid(FluidTags.WATER) || entity.isEyeInFluid(FluidTags.LAVA)
        }

    }

    private fun canTransitionToAir(driver: LivingEntity?): Boolean {
        if (driver != null) {
            if (driver !is DoubleJump) return false
            if (behaviours[RidingStyle.AIR] == null) return false
            // check if style is water and player is submerged
            val isEyeInLiquid = entity.isEyeInFluid(FluidTags.WATER) || entity.isEyeInFluid(FluidTags.LAVA)
            if (context?.style == RidingStyle.LIQUID && isEyeInLiquid) return false
            return (driver as DoubleJump).isDoubleJumping
        }
        else {
            return !entity.onGround() && !(entity.isInLiquid || entity.isUnderWater)
        }
    }
}

class ActiveRidingContext(
    val behaviour: ResourceLocation,
    val settings: RidingBehaviourSettings,
    val state: RidingBehaviourState,
    val style: RidingStyle
)