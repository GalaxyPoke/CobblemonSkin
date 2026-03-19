/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour.types.air

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.MoLangMath.lerp
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.CobblemonRideSettings
import com.cobblemon.mod.common.OrientationControllable
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourState
import com.cobblemon.mod.common.api.riding.behaviour.Side
import com.cobblemon.mod.common.api.riding.behaviour.ridingState
import com.cobblemon.mod.common.api.riding.posing.PoseOption
import com.cobblemon.mod.common.api.riding.posing.PoseProvider
import com.cobblemon.mod.common.api.riding.sound.RideSoundSettingsList
import com.cobblemon.mod.common.api.riding.stats.RidingStat
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.blockPositionsAsListRounded
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.math.geometry.toRadians
import com.cobblemon.mod.common.util.readNullableExpression
import com.cobblemon.mod.common.util.readRidingStats
import com.cobblemon.mod.common.util.resolveDouble
import com.cobblemon.mod.common.util.toVec3d
import com.cobblemon.mod.common.util.writeNullableExpression
import com.cobblemon.mod.common.util.writeRidingStats
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes
import org.joml.Vector3f

class JetBehaviour : RidingBehaviour<JetSettings, JetState> {
    companion object {
        val KEY = cobblemonResource("air/jet")
    }

    override val key = KEY

    override fun getRidingStyle(settings: JetSettings, state: JetState): RidingStyle {
        return RidingStyle.AIR
    }

    val globalJet: JetSettings
        get() = CobblemonRideSettings.jet

    val poseProvider = PoseProvider<JetSettings, JetState>(PoseType.HOVER)
        .with(PoseOption(PoseType.FLY) { _, state, _ -> state.rideVelocity.get().z > 0.1 })

    override fun isActive(settings: JetSettings, state: JetState, vehicle: PokemonEntity): Boolean {
        return Shapes.create(vehicle.boundingBox).blockPositionsAsListRounded().any {
            //Need to check other fluids
            if (vehicle.isInWater || vehicle.isUnderWater) {
                return@any false
            }
            if (it.y.toDouble() == (vehicle.position().y)) {
                val blockState = vehicle.level().getBlockState(it.below())
                return@any !(!blockState.isAir && blockState.fluidState.isEmpty)
            }
            true
        }
    }

    override fun pose(settings: JetSettings, state: JetState, vehicle: PokemonEntity): PoseType {
        return poseProvider.select(settings, state, vehicle)
    }

    override fun speed(settings: JetSettings, state: JetState, vehicle: PokemonEntity, driver: Player): Float {
        return state.rideVelocity.get().length().toFloat()
    }

    override fun tick(settings: JetSettings, state: JetState, vehicle: PokemonEntity, driver: Player, input: Vec3) {
        if(vehicle.level().isClientSide) {
            handleBoosting(state)
            tickStamina(settings, state, vehicle)
        }
    }

    fun handleBoosting(
        state: JetState,
    ) {
        //If the forward key is not held then it cannot be boosting
        val boostKeyPressed = Minecraft.getInstance().options.keySprint.isDown()

        if(state.stamina.get() != 0.0f && boostKeyPressed) {
            if (state.stamina.get() >= 0.0f) {
                //If on the previous tick the boost key was held then don't change if the ride is boosting
                if(state.boostIsToggleable.get()) {
                    //flip the boosting state if boost key is pressed
                    state.boosting.set(!state.boosting.get())
                }
                //If the boost key is not held then next tick boosting is toggleable
                state.boostIsToggleable.set(false)
            }
        } else {
            //Turn off boost and reset boost params
            state.boostIsToggleable.set(true)
            state.boosting.set(false)
            state.canSpeedBurst.set(true)
        }
    }

    fun tickStamina(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity,
    ) {
        val stam = state.stamina.get()
        var newStam = stam
        val stamDrainRate = (1.0f / vehicle.runtime.resolveDouble(settings.staminaExpr ?: globalJet.staminaExpr!!)).toFloat() / 20.0f

        if (state.boosting.get()) {
            newStam = max(0.0f,stam - stamDrainRate * 1.5f)
        } else {
            newStam = max(0.0f,stam - stamDrainRate)
        }

        // If out of stamina then increase noStamTickCnt
        if (newStam == 0.0f) {
            state.noStamTickCnt.set(state.noStamTickCnt.get() + 1)
        } else {
            state.noStamTickCnt.get()
        }

        state.stamina.set(newStam)
    }

    override fun rotation(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {
        return Vec2(driver.xRot * 0.5f, driver.yRot)
    }

    override fun velocity(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        val controller = (vehicle as? OrientationControllable)?.orientationController
        if (controller == null || controller.orientation == null) return Vec3.ZERO

        //Calculate ride space velocity
        calculateRideSpaceVel(settings, state, vehicle, driver)

        // The downward force used to encourage players to stop flying upside down.
        val penaltyDeccel = vehicle.runtime.resolveDouble(settings.maxDownwardForceSeconds ?: globalJet.maxDownwardForceSeconds!!).toFloat() * 20.0f
        val extraDownwardForce = if(state.stamina.get() == 0.0f) -0.3 * (state.noStamTickCnt.get() / penaltyDeccel).coerceIn(0.0f, 1.0f) else 0.0 // 6 blocks a second downward

        // Convert the local velocity vector into a world vector
        val localVelVec = Vector3f(
            state.rideVelocity.get().x.toFloat(),
            (state.rideVelocity.get().y).toFloat(),
            state.rideVelocity.get().z.toFloat() * -1.0f // Flip the z axis to make this left handed? orr.. right handed? idk, flip it though
        )
        var worldVelVec = localVelVec.mul(controller.orientation).toVec3d().yRot(vehicle.yRot.toRadians()) // Unrotate preemptively as this vector gets rotate later down the line in MC logic.
        worldVelVec =  Vec3(
            worldVelVec.x,
            worldVelVec.y * (1 - (state.noStamTickCnt.get() / penaltyDeccel).coerceIn(0.0f, 1.0f)), // Dampen the upwards movement depending on stamina depletion time
            worldVelVec.z).add(0.0, extraDownwardForce, 0.0) // Add the stamina depletion force to bring the ride down.

        return worldVelVec
    }

    /*
    *  Calculates the change in the ride space vector due to player input and ride state
    */
    private fun calculateRideSpaceVel(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity,
        driver: Player
    ) {
        val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr ?: globalJet.speedExpr!!) / 20.0
        val accel = topSpeed / (vehicle.runtime.resolveDouble(settings.accelerationExpr ?: globalJet.accelerationExpr!!) * 20.0)
        val deccel = vehicle.runtime.resolveDouble(settings.deccelRate ?: globalJet.deccelRate!!)//0.005
        val minSpeed = topSpeed * vehicle.runtime.resolveDouble(settings.minSpeedFactor ?: globalJet.minSpeedFactor!!)
        val speed = state.rideVelocity.get().z
        val boostMult = vehicle.runtime.resolveDouble(settings.jumpExpr ?: globalJet.jumpExpr!!)
        val maxNoStamickCnt = vehicle.runtime.resolveDouble(settings.maxDownwardForceSeconds ?: globalJet.maxDownwardForceSeconds!!).toFloat() * 20.0f


        val boostTopSpeed = topSpeed * boostMult
        val boostAccel = accel * boostMult

        //speed up and slow down based on input
        if (state.stamina.get() == 0.0f) {
            // Decelerate currently always a constant half of max acceleration.

            // Increase penalty deccel when stamina has been depleted for a while.
            val penaltyDeccel = deccel * (state.noStamTickCnt.get() / maxNoStamickCnt).coerceIn(0.0f, 1.0f)
            val minPenaltySpeed = minSpeed
            state.rideVelocity.set(
                Vec3(
                    state.rideVelocity.get().x,
                    state.rideVelocity.get().y,
                    max(state.rideVelocity.get().z - (penaltyDeccel), minPenaltySpeed)
                )
            )
        }
        else if (state.boosting.get() && speed < boostTopSpeed) {
            state.rideVelocity.set(
                Vec3(
                    state.rideVelocity.get().x,
                    state.rideVelocity.get().y,
                    min(state.rideVelocity.get().z + (boostAccel), boostTopSpeed)
                )
            )
        } else if ((speed < minSpeed) || (driver.zza > 0.0 && speed < topSpeed)) {
            //modify acceleration to be slower when at closer speeds to top speed
            val accelMod = max(-(RidingBehaviour.scaleToRange(speed, minSpeed, topSpeed)) + 1, 0.0)
            state.rideVelocity.set(
                Vec3(
                    state.rideVelocity.get().x,
                    state.rideVelocity.get().y,
                    min(state.rideVelocity.get().z + (accel * accelMod), topSpeed)
                )
            )
        } else if (driver.zza < 0.0 && speed > minSpeed) {
            // Decelerate currently always a constant half of max acceleration.
            state.rideVelocity.set(
                Vec3(
                    state.rideVelocity.get().x,
                    state.rideVelocity.get().y,
                    max(state.rideVelocity.get().z - (deccel), minSpeed)
                )
            )
        } else if (speed > topSpeed) {
            state.rideVelocity.set(
                state.rideVelocity.get().scale(0.98)
            )
        }

        /****************************************************************************
         * Kill strafing velocities carried over from controller transition. Don't
         * kill immediately to give some momentum to transitions.
         ***************************************************************************/
        state.rideVelocity.set(Vec3(
            lerp(state.rideVelocity.get().x, 0.0, 0.03),
            lerp(state.rideVelocity.get().y, 0.0, 0.03),
            state.rideVelocity.get().z
        ))

    }

    override fun angRollVel(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {

        //Cap at a rate of 5fps so frame skips dont lead to huge jumps
        val cappedDeltaTime = min(deltaTime, 0.2)

        //Get handling in degrees per second
        val yawRotRate = vehicle.runtime.resolveDouble(settings.handlingYawExpr ?: globalJet.handlingYawExpr!!)

        //Base the change off of deltatime.
        var handlingYaw = yawRotRate * (cappedDeltaTime)

        //apply stamina debuff if applicable
        handlingYaw *= if (state.stamina.get() > 0.0) 1.0 else 0.5

        //A+D to yaw
        val yawForce = driver.xxa * handlingYaw * -1

        return Vec3(yawForce, 0.0, 0.0)
    }

    override fun rotationOnMouseXY(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity,
        driver: Player,
        mouseY: Double,
        mouseX: Double,
        mouseYSmoother: SmoothDouble,
        mouseXSmoother: SmoothDouble,
        sensitivity: Double,
        deltaTime: Double
    ): Vec3 {
        if (vehicle !is OrientationControllable) return Vec3.ZERO

        val cappedDeltaTime = min(deltaTime, 0.2)

        // Accumulate the mouse input
        state.currMouseXForce.set((state.currMouseXForce.get() + (0.0015 * mouseX)).coerceIn(-1.0, 1.0))
        state.currMouseYForce.set((state.currMouseYForce.get() + (0.0015 * mouseY)).coerceIn(-1.0, 1.0))

        //Get handling in degrees per second
        val handlingDebuff = if(state.stamina.get() == 0.0f) 0.5 else 1.0
        var handling = vehicle.runtime.resolveDouble(settings.handlingExpr ?: globalJet.handlingExpr!!) * handlingDebuff

        //convert it to delta time
        handling *= (cappedDeltaTime)

        val pitchRot = handling * state.currMouseYForce.get()

        // Roll
        val rollRot = handling * state.currMouseXForce.get()

        val mouseRotation = Vec3(0.0, pitchRot, rollRot)

        // Have accumulated input begin decay when no input detected
        if (abs(mouseX) == 0.0) {
            // Have decay on roll be much stronger.
            state.currMouseXForce.set(lerp(state.currMouseXForce.get(), 0.0, 0.02))
        }
        if (mouseY == 0.0) {
            state.currMouseYForce.set(lerp(state.currMouseYForce.get(), 0.0, 0.02))
        }

        //yaw, pitch, roll
        return mouseRotation
    }

    override fun canJump(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return false
    }

    override fun setRideBar(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return (state.stamina.get() / 1.0f)
    }

    override fun jumpForce(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun gravity(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return 0.0
    }

    override fun rideFovMultiplier(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        val penaltyDeccel = vehicle.runtime.resolveDouble(settings.maxDownwardForceSeconds ?: globalJet.maxDownwardForceSeconds!!).toFloat() * 20.0f
        return when {
            state.noStamTickCnt.get() != 0 -> 1f - 0.2f*(state.noStamTickCnt.get() / penaltyDeccel).coerceIn(0.0f,1.0f)
            state.boosting.get() -> 1.2f
            else -> 1.0f
        }
    }

    override fun useAngVelSmoothing(settings: JetSettings, state: JetState, vehicle: PokemonEntity): Boolean {
        return true
    }

    override fun inertia(settings: JetSettings, state: JetState, vehicle: PokemonEntity): Double {
        return 1.0
    }

    override fun shouldRoll(settings: JetSettings, state: JetState, vehicle: PokemonEntity): Boolean {
        return true
    }

    override fun turnOffOnGround(settings: JetSettings, state: JetState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun dismountOnShift(settings: JetSettings, state: JetState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotateRiderHead(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun getRideSounds(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity
    ): RideSoundSettingsList {
        return settings.rideSounds
    }

    override fun createDefaultState(settings: JetSettings) = JetState()

    override fun damageOnCollision(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity,
        impactVec: Vec3
    ): Boolean {
        val impactSpeed = impactVec.horizontalDistance().toFloat() * 10f
        return vehicle.causeFallDamage(impactSpeed, 1f, vehicle.damageSources().flyIntoWall())
    }

    override fun asMoLangValue(
        settings: JetSettings,
        state: JetState,
        vehicle: PokemonEntity
    ): ObjectValue<RidingBehaviour<JetSettings, JetState>> {
        val value = super.asMoLangValue(settings, state, vehicle)
        value.functions.put("boosting") { DoubleValue(state.boosting.get()) }
        value.functions.put("can_speed_burst") { DoubleValue(state.canSpeedBurst.get()) }
        return value
    }
}

class JetSettings : RidingBehaviourSettings {
    override val key = JetBehaviour.KEY
    override val stats = mutableMapOf<RidingStat, IntRange>()

    var gravity: Expression? = null
        private set

    var deccelRate: Expression? = null
        private set

    // Mult to top speed in order to derive minSpeed
    var minSpeedFactor: Expression? = null
        private set

    var handlingYawExpr: Expression? = null
        private set

    // Make configurable by json
    var infiniteStamina: Expression? = null
        private set

    // Boost power. Mult for top speed and accel while boosting
    var jumpExpr: Expression? = null
        private set

    // Turn rate in degrees per second
    var handlingExpr: Expression? = null
        private set
    // Top Speed in blocks per second
    var speedExpr: Expression? = null
        private set
    // Seconds to get to top speed
    var accelerationExpr: Expression? = null
        private set
    // Time in seconds to drain full bar of stamina flying
    var staminaExpr: Expression? = null
        private set

    var maxDownwardForceSeconds: Expression? = null
        private set

    var rideSounds: RideSoundSettingsList = RideSoundSettingsList()

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeRidingStats(stats)
        rideSounds.encode(buffer)
        buffer.writeNullableExpression(gravity)
        buffer.writeNullableExpression(deccelRate)
        buffer.writeNullableExpression(minSpeedFactor)
        buffer.writeNullableExpression(handlingYawExpr)
        buffer.writeNullableExpression(infiniteStamina)
        buffer.writeNullableExpression(jumpExpr)
        buffer.writeNullableExpression(handlingExpr)
        buffer.writeNullableExpression(speedExpr)
        buffer.writeNullableExpression(accelerationExpr)
        buffer.writeNullableExpression(staminaExpr)
        buffer.writeNullableExpression(maxDownwardForceSeconds)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        stats.putAll(buffer.readRidingStats())
        rideSounds = RideSoundSettingsList.decode(buffer)
        gravity = buffer.readNullableExpression()
        deccelRate = buffer.readNullableExpression()
        minSpeedFactor = buffer.readNullableExpression()
        handlingYawExpr = buffer.readNullableExpression()
        infiniteStamina = buffer.readNullableExpression()
        jumpExpr = buffer.readNullableExpression()
        handlingExpr = buffer.readNullableExpression()
        speedExpr = buffer.readNullableExpression()
        accelerationExpr = buffer.readNullableExpression()
        staminaExpr = buffer.readNullableExpression()
        maxDownwardForceSeconds = buffer.readNullableExpression()
    }
}

class JetState : RidingBehaviourState() {
    var currSpeed = ridingState(0.0, Side.CLIENT)
    var currMouseXForce = ridingState(0.0, Side.CLIENT)
    var currMouseYForce = ridingState(0.0, Side.CLIENT)
    var boosting = ridingState(false, Side.BOTH)
    var boostIsToggleable = ridingState(false, Side.BOTH)
    var canSpeedBurst = ridingState(false, Side.BOTH)
    var noStamTickCnt = ridingState(0, Side.CLIENT) // Value that is increased for every tick you are out of stamina

    override fun encode(buffer: FriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeDouble(currSpeed.get())
        buffer.writeFloat(stamina.get())
        buffer.writeBoolean(boosting.get())
        buffer.writeBoolean(boostIsToggleable.get())
        buffer.writeBoolean(canSpeedBurst.get())
    }

    override fun decode(buffer: FriendlyByteBuf) {
        super.decode(buffer)
        currSpeed.set(buffer.readDouble(), forced = true)
        boosting.set(buffer.readBoolean(), forced = true)
        boostIsToggleable.set(buffer.readBoolean(), forced = true)
        canSpeedBurst.set(buffer.readBoolean(), forced = true)
    }

    override fun reset() {
        super.reset()
        currSpeed.set(0.0, forced = true)
        currMouseXForce.set(0.0, forced = true)
        currMouseYForce.set(0.0, forced = true)
        boosting.set(false, forced = true)
        boostIsToggleable.set(false, forced = true)
        canSpeedBurst.set(true, forced = true)
        noStamTickCnt.set(0, forced = true)
    }

    override fun copy() = JetState().also {
        it.currSpeed.set(currSpeed.get(), forced = true)
        it.stamina.set(stamina.get(), forced = true)
        it.rideVelocity.set(rideVelocity.get(), forced = true)
        it.boosting.set(this.boosting.get(), forced = true)
        it.boostIsToggleable.set(this.boosting.get(), forced = true)
        it.canSpeedBurst.set(this.canSpeedBurst.get(), forced = true)
    }

    override fun shouldSync(previous: RidingBehaviourState): Boolean {
        if (previous !is JetState) return false
        if (previous.currSpeed != currSpeed) return true
        return super.shouldSync(previous)
    }
}
