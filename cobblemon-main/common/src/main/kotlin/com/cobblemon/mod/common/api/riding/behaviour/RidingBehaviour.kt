/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.api.riding.sound.RideSoundSettingsList
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

/**
 * Represents the behaviour of a Pokemon when being ridden.
 * This is intended to contain the logic for how a Pokemon should behave when being ridden.
 *
 * This class should remain stateless as it is intended to be shared between multiple instances of Pokemon.
 *
 * @author landonjw
 */
interface RidingBehaviour<Settings : RidingBehaviourSettings, State : RidingBehaviourState> {
    val key: ResourceLocation

    fun getRidingStyle(settings: Settings, state: State): RidingStyle

    fun isActive(settings: Settings, state: State, vehicle: PokemonEntity): Boolean

    fun tick(settings: Settings, state: State, vehicle: PokemonEntity, driver: Player, input: Vec3) {}

    fun pose(settings: Settings, state: State, vehicle: PokemonEntity): PoseType

    fun speed(settings: Settings, state: State, vehicle: PokemonEntity, driver: Player): Float

    fun clampPassengerRotation(settings: Settings, state: State, vehicle: PokemonEntity, driver: LivingEntity) {}

    fun updatePassengerRotation(settings: Settings, state: State, vehicle: PokemonEntity, driver: LivingEntity) {}

    fun rotation(settings: Settings, state: State, vehicle: PokemonEntity, driver: LivingEntity): Vec2

    fun velocity(settings: Settings, state: State, vehicle: PokemonEntity, driver: Player, input: Vec3): Vec3

    fun angRollVel(settings: Settings, state: State, vehicle: PokemonEntity, driver: Player, deltaTime: Double): Vec3

    fun rotationOnMouseXY(
        settings: Settings,
        state: State,
        vehicle: PokemonEntity,
        driver: Player,
        mouseY: Double,
        mouseX: Double,
        mouseYSmoother: SmoothDouble,
        mouseXSmoother: SmoothDouble,
        sensitivity: Double,
        deltaTime: Double
    ): Vec3

    fun canJump(settings: Settings, state: State, vehicle: PokemonEntity, driver: Player): Boolean

    fun setRideBar(settings: Settings, state: State, vehicle: PokemonEntity, driver: Player): Float

    fun jumpForce(settings: Settings, state: State, vehicle: PokemonEntity, driver: Player, jumpStrength: Int): Vec3

    fun gravity(settings: Settings, state: State, vehicle: PokemonEntity, regularGravity: Double): Double

    fun rideFovMultiplier(settings: Settings, state: State, vehicle: PokemonEntity, driver: Player): Float

    fun useAngVelSmoothing(settings: Settings, state: State, vehicle: PokemonEntity): Boolean

    fun inertia(settings: Settings, state: State, vehicle: PokemonEntity): Double

    fun shouldRoll(settings: Settings, state: State, vehicle: PokemonEntity): Boolean

    fun turnOffOnGround(settings: Settings, state: State, vehicle: PokemonEntity): Boolean

    fun dismountOnShift(settings: Settings, state: State, vehicle: PokemonEntity): Boolean

    fun shouldRotatePokemonHead(settings: Settings, state: State, vehicle: PokemonEntity): Boolean

    fun shouldRotateRiderHead(settings: Settings, state: State, vehicle: PokemonEntity): Boolean

    fun getRideSounds(settings: Settings, state: State, vehicle: PokemonEntity): RideSoundSettingsList

    fun maxUpStep(settings: Settings, state: State, vehicle: PokemonEntity): Float? = null

    fun canStopRiding(settings: Settings, state: State, vehicle: PokemonEntity, passenger: Player): Boolean = true

    fun createDefaultState(settings: Settings): State

    /**
     * Calculate the damage a horizontal collision will do to a ridden Pokémon. This is only relevant to fast flying behaviours, fall damage is separate!
     */
    fun damageOnCollision(settings: Settings, state: State, vehicle: PokemonEntity, impactVec: Vec3): Boolean = false

    /**
     * Called in [com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer] to allow the controller to set rotations
     * through code using partialTicks and right before rendering.
     * //TODO: This needs to be further refined. Maybe only the orientationController needs affected? Maybe the rotations set here don't need to be applied to the controller/camera
     */
    fun applyRenderRotation(settings: Settings, state: State, vehicle: PokemonEntity, partialTicks: Float) {}

    /**
     * Determines if the pair of mouse inputs Pair(mouseX, mouseY) get used to modify the driverRotations or the vehicle rotations themselves.
     * Driver rotations are the rotations used by passengers or a driver that is freelooking. This does not override the freelook button.
     */
    fun mouseModifiesDriverRotation(settings: Settings, state: State, vehicle: PokemonEntity): Pair<Boolean, Boolean> = Pair(false, false)

    /**
     * Used in MoLang functions for querying states for animations or other magical MoLang effects.
     */
    fun asMoLangValue(settings: Settings, state: State, vehicle: PokemonEntity): ObjectValue<RidingBehaviour<Settings, State>> {
        val value = ObjectValue(
            this
        )
        value.functions.put("ride_velocity") { state.rideVelocity.get().asMoLangValue() }
        value.functions.put("stamina") { DoubleValue(state.stamina.get()) }
        return value
    }

    /**
     * Internal helpers to help behaviour specific calculations
     */
    companion object {
        /**
         *  Scales a given value between a min and a max.
         *  The result is clamped between 0.0 and 1.0, where 0.0 represents x is at or below min
         *  and 1.0 represents x is at or above it.
         */
        internal fun scaleToRange(x: Double, min: Double, max: Double): Double {
            return if ((max - min) < 0.01) 0.0 else ((x - min) / (max - min)).coerceIn(0.0, 1.0)
        }
    }
}
