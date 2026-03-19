/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour.types.composite

import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.api.riding.behaviour.*
import com.cobblemon.mod.common.api.riding.behaviour.types.composite.strategies.CompositeRidingStrategies
import com.cobblemon.mod.common.api.riding.sound.RideSoundSettingsList
import com.cobblemon.mod.common.api.riding.stats.RidingStat
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.adapters.RidingBehaviourSettingsAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readRidingStats
import com.cobblemon.mod.common.util.writeRidingStats
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

class CompositeBehaviour : RidingBehaviour<CompositeSettings, CompositeState> {
    companion object {
        val KEY = cobblemonResource("composite")
    }

    override val key = KEY

    override fun getRidingStyle(settings: CompositeSettings, state: CompositeState): RidingStyle {
        return chooseBehaviour(settings, state) {
            behaviour, behaviourSettings, behaviourState ->
            behaviour.getRidingStyle(behaviourSettings, behaviourState)
        }
    }
    
    override fun createDefaultState(settings: CompositeSettings): CompositeState {
        val defaultBehaviour = RidingBehaviours.get(settings.defaultBehaviour.key)
        val defaultState = defaultBehaviour.createDefaultState(settings.defaultBehaviour)
        val alternateBehaviour = RidingBehaviours.get(settings.alternateBehaviour.key)
        val alternativeState = alternateBehaviour.createDefaultState(settings.alternateBehaviour)
        return CompositeState(
            defaultBehaviour = settings.defaultBehaviour.key,
            defaultBehaviourState = defaultState,
            alternateBehaviourState = alternativeState
        )
    }

    fun <T> chooseBehaviour(
        settings: CompositeSettings,
        state: CompositeState,
        action: (behaviour: RidingBehaviour<RidingBehaviourSettings, RidingBehaviourState>, settings: RidingBehaviourSettings, state: RidingBehaviourState) -> T
    ): T {
        val defaultBehaviour = RidingBehaviours.get(settings.defaultBehaviour.key)
        val alternativeBehaviour = RidingBehaviours.get(settings.alternateBehaviour.key)
        return when (state.activeBehaviour.get()) {
            settings.defaultBehaviour.key -> action(
                defaultBehaviour,
                settings.defaultBehaviour,
                state.defaultBehaviourState
            )
            settings.alternateBehaviour.key -> action(
                alternativeBehaviour,
                settings.alternateBehaviour,
                state.alternateBehaviourState
            )

            else -> error("Invalid controller: ${state.activeBehaviour.get()}")
        }
    }

    override fun tick(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ) {
        val strategy = CompositeRidingStrategies.get(settings.transitionStrategy)
        strategy.tick(settings, state, state.defaultBehaviourState, state.alternateBehaviourState, vehicle, driver, input)
    }

    override fun clampPassengerRotation(
        settings: CompositeSettings,
        state: CompositeState,
        entity: PokemonEntity,
        driver: LivingEntity
    ) {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.clampPassengerRotation(behaviourSettings, behaviourState, entity, driver)
        }
    }

    override fun updatePassengerRotation(
        settings: CompositeSettings,
        state: CompositeState,
        entity: PokemonEntity,
        driver: LivingEntity
    ) {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.updatePassengerRotation(behaviourSettings, behaviourState, entity, driver)
        }
    }

    override fun shouldRotateRiderHead(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity
    ): Boolean {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.shouldRotateRiderHead(behaviourSettings, behaviourState, vehicle)
        }
    }

    override fun shouldRotatePokemonHead(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity
    ): Boolean {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.shouldRotatePokemonHead(behaviourSettings, behaviourState, vehicle)
        }
    }

    override fun dismountOnShift(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity
    ): Boolean {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.dismountOnShift(behaviourSettings, behaviourState, vehicle)
        }
    }

    override fun turnOffOnGround(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity
    ): Boolean {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.turnOffOnGround(behaviourSettings, behaviourState, vehicle)
        }
    }

    override fun shouldRoll(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity
    ): Boolean {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.shouldRoll(behaviourSettings, behaviourState, vehicle)
        }
    }

    override fun inertia(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity
    ): Double {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.inertia(behaviourSettings, behaviourState, vehicle)
        }
    }

    override fun useAngVelSmoothing(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity
    ): Boolean {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.useAngVelSmoothing(behaviourSettings, behaviourState, vehicle)
        }
    }

    override fun rideFovMultiplier(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.rideFovMultiplier(behaviourSettings, behaviourState, vehicle, driver)
        }
    }

    override fun gravity(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.gravity(behaviourSettings, behaviourState, vehicle, regularGravity)
        }
    }

    override fun jumpForce(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.jumpForce(behaviourSettings, behaviourState, vehicle, driver, jumpStrength)
        }
    }

    override fun setRideBar(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.setRideBar(behaviourSettings, behaviourState, vehicle, driver)
        }
    }

    override fun canJump(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.canJump(behaviourSettings, behaviourState, vehicle, driver)
        }
    }

    override fun rotationOnMouseXY(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity,
        driver: Player,
        mouseY: Double,
        mouseX: Double,
        mouseYSmoother: SmoothDouble,
        mouseXSmoother: SmoothDouble,
        sensitivity: Double,
        deltaTime: Double
    ): Vec3 {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.rotationOnMouseXY(
                behaviourSettings,
                behaviourState,
                vehicle,
                driver,
                mouseY,
                mouseX,
                mouseYSmoother,
                mouseXSmoother,
                sensitivity,
                deltaTime
            )
        }
    }

    override fun angRollVel(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.angRollVel(behaviourSettings, behaviourState, vehicle, driver, deltaTime)
        }
    }

    override fun velocity(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.velocity(behaviourSettings, behaviourState, vehicle, driver, input)
        }
    }

    override fun rotation(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.rotation(behaviourSettings, behaviourState, vehicle, driver)
        }
    }

    override fun speed(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.speed(behaviourSettings, behaviourState, vehicle, driver)
        }
    }

    override fun pose(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity
    ): PoseType {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.pose(behaviourSettings, behaviourState, vehicle)
        }
    }

    override fun getRideSounds(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity
    ): RideSoundSettingsList {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.getRideSounds(behaviourSettings, behaviourState, vehicle)
        }
    }

    override fun isActive(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity
    ): Boolean {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.isActive(behaviourSettings, behaviourState, vehicle)
        }
    }

    override fun canStopRiding(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity,
        passenger: Player
    ): Boolean {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.canStopRiding(behaviourSettings, behaviourState, vehicle, passenger)
        }
    }

    override fun damageOnCollision(
        settings: CompositeSettings,
        state: CompositeState,
        vehicle: PokemonEntity,
        impactVec: Vec3
    ): Boolean {
        return chooseBehaviour(settings, state) { behaviour, behaviourSettings, behaviourState ->
            behaviour.damageOnCollision(behaviourSettings, behaviourState, vehicle, impactVec)
        }
    }
}

open class CompositeSettings : RidingBehaviourSettings {
    lateinit var transitionStrategy: ResourceLocation
        private set
    lateinit var defaultBehaviour: RidingBehaviourSettings
        private set
    lateinit var alternateBehaviour: RidingBehaviourSettings
        private set

    override val key: ResourceLocation = CompositeBehaviour.KEY
    override val stats = mutableMapOf<RidingStat, IntRange>()

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeRidingStats(stats)
        buffer.writeResourceLocation(transitionStrategy)
        buffer.writeResourceLocation(defaultBehaviour.key)
        defaultBehaviour.encode(buffer)
        buffer.writeResourceLocation(alternateBehaviour.key)
        alternateBehaviour.encode(buffer)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        stats.putAll(buffer.readRidingStats())
        transitionStrategy = buffer.readResourceLocation()
        val defaultBehaviourKey = buffer.readResourceLocation()
        defaultBehaviour = RidingBehaviourSettingsAdapter.types[defaultBehaviourKey]?.getConstructor()?.newInstance()
            ?: error("Unknown controller key: $key")
        defaultBehaviour.decode(buffer)
        val alternativeBehaviourKey = buffer.readResourceLocation()
        alternateBehaviour =
            RidingBehaviourSettingsAdapter.types[alternativeBehaviourKey]?.getConstructor()?.newInstance()
                ?: error("Unknown controller key: $key")
        alternateBehaviour.decode(buffer)
    }
}

class CompositeState(
    val defaultBehaviour: ResourceLocation,
    val defaultBehaviourState: RidingBehaviourState,
    val alternateBehaviourState: RidingBehaviourState
) : RidingBehaviourState() {
    var activeBehaviour = ridingState(defaultBehaviour, Side.CLIENT)
    var lastTransition = ridingState(-100L, Side.BOTH)

    override val rideVelocity: SidedRidingState<Vec3>
        get() = when (activeBehaviour.get()) {
            defaultBehaviour -> defaultBehaviourState.rideVelocity
            else -> alternateBehaviourState.rideVelocity
        }

    override val stamina: SidedRidingState<Float>
        get() = when (activeBehaviour.get()) {
            defaultBehaviour -> defaultBehaviourState.stamina
            else -> alternateBehaviourState.stamina
        }

    override fun reset() {
        super.reset()
        activeBehaviour.set(defaultBehaviour, forced = true)
        lastTransition.set(-100L, forced = true)
        defaultBehaviourState.reset()
        alternateBehaviourState.reset()
    }

    override fun copy(): CompositeState {
        val state =
            CompositeState(defaultBehaviour, defaultBehaviourState.copy(), alternateBehaviourState.copy())
        state.activeBehaviour.set(activeBehaviour.get(), forced = true)
        state.lastTransition.set(lastTransition.get(), forced = true)
        state.rideVelocity.set(rideVelocity.get(), forced = true)
        state.stamina.set(stamina.get(), forced = true)
        return state
    }

    override fun shouldSync(previous: RidingBehaviourState): Boolean {
        if (previous !is CompositeState) return false
        if (previous.activeBehaviour.get() != activeBehaviour.get()) return true
        if (defaultBehaviourState.shouldSync(previous.defaultBehaviourState)) return true
        if (alternateBehaviourState.shouldSync(previous.alternateBehaviourState)) return true
        return super.shouldSync(previous)
    }

    override fun encode(buffer: FriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeResourceLocation(activeBehaviour.get())
        defaultBehaviourState.encode(buffer)
        alternateBehaviourState.encode(buffer)
    }

    override fun decode(buffer: FriendlyByteBuf) {
        super.decode(buffer)
        activeBehaviour.set(buffer.readResourceLocation(), forced = true)
        defaultBehaviourState.decode(buffer)
        alternateBehaviourState.decode(buffer)
    }

}
