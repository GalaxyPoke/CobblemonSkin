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
import com.cobblemon.mod.common.api.orientation.OrientationController
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.api.riding.behaviour.*
import com.cobblemon.mod.common.api.riding.posing.PoseOption
import com.cobblemon.mod.common.api.riding.posing.PoseProvider
import com.cobblemon.mod.common.api.riding.sound.RideSoundSettingsList
import com.cobblemon.mod.common.api.riding.stats.RidingStat
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.*
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.*

class BirdBehaviour : RidingBehaviour<BirdSettings, BirdState> {
    companion object {
        val KEY = cobblemonResource("air/bird")
    }

    override val key: ResourceLocation = KEY

    override fun getRidingStyle(settings: BirdSettings, state: BirdState): RidingStyle {
        return RidingStyle.AIR
    }

    val poseProvider = PoseProvider<BirdSettings, BirdState>(PoseType.HOVER)
        .with(PoseOption(PoseType.FLY) { _, state, _ -> state.rideVelocity.get().z > 0.2 })

    val globalBird: BirdSettings
        get() = CobblemonRideSettings.bird

    override fun isActive(settings: BirdSettings, state: BirdState, vehicle: PokemonEntity): Boolean {
        return !(vehicle.isUnderWater)
    }

    override fun pose(settings: BirdSettings, state: BirdState, vehicle: PokemonEntity): PoseType {
        return poseProvider.select(settings, state, vehicle)
    }

    override fun speed(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return state.rideVelocity.get().length().toFloat()
    }

    override fun tick(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ) {
        if(vehicle.level().isClientSide) {
            tickStamina(settings, state, vehicle, driver)
            checkUpsideDown(state, vehicle, driver)
        }
    }

    private fun checkUpsideDown(
        state: BirdState,
        vehicle: PokemonEntity,
        driver: Player,
    ) {
        // Also checks stamina
        val controller = (vehicle as OrientationControllable).orientationController
        if (cos(controller.roll.toRadians()) < 0.0) {
            val upsideDownRate = 1.0 / (2.0 * 20.0) // 2 seconds to max out upsideDown force
            state.currUpsideDownForce.set( min(1.0, state.currUpsideDownForce.get() + upsideDownRate * abs(cos(controller.roll.toRadians()))))
        } else if (state.stamina.get() == 0.0f) { // checks stamina depletion and forces dive if depleted
            val upsideDownRate = 1.0 / (2.0 * 20.0) // 1 seconds to max out
            state.currUpsideDownForce.set( min(1.0, state.currUpsideDownForce.get() + upsideDownRate))
        } else { // Its right side up or has stamina so reset it
            state.currUpsideDownForce.set(0.0)
        }
    }

    fun tickStamina(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity,
        driver: Player
    ) {
        val stam = state.stamina.get()
        val controller = (driver as OrientationControllable).orientationController

        if (vehicle.runtime.resolveBoolean(settings.infiniteStamina ?: globalBird.infiniteStamina!!)) {
            return
        }

        // Grab the boost time in seconds and convert to ticks. Then calculate the drain rate as inversely
        // proportional to the number of ticks of boost thus making a full boost take x ticks
        // in short: "Stamina drains slower at higher values and also replenishes slower"
        val totalStam = vehicle.runtime.resolveDouble(settings.staminaExpr ?: globalBird.staminaExpr!!) * 20.0f
        val horzMod = vehicle.runtime.resolveDouble(settings.stamDrainHorzMod ?: globalBird.stamDrainHorzMod!!)
        val glideMod = vehicle.runtime.resolveFloat(settings.stamDrainPercRateGlide ?: globalBird.stamDrainPercRateGlide!!)
        val stamDrainPerRateDive = vehicle.runtime.resolveDouble(settings.stamDrainPerRateDive ?: globalBird.stamDrainPerRateDive!!).coerceIn(0.0, 1.0) // Ensure its between 1 and 0 or things get weird

        val stamDrainHorz = if(vehicle.deltaMovement.horizontalDistance() < 1e-6) horzMod else max(1.0 + ((horzMod - 1.0) * atan( vehicle.deltaMovement.y / vehicle.deltaMovement.horizontalDistance())), 1.0)
        val diveAmount = sin(controller.pitch.toRadians()).coerceIn(0.0f, 1.0f)// from 0.0 -> 1.0 how steep is the current dive angle
        val diveMod = ((1.0 - diveAmount) * ( 1.0 - stamDrainPerRateDive ) + stamDrainPerRateDive).toFloat() // smooth between 1.0 and the dive drain mod depending upon how dived the ride is
        val stamDrainRate = (1.0f / totalStam).toFloat() * stamDrainHorz.toFloat()

        val newStam = if (driver.deltaMovement.length() > 0.1) max(0.0f,stam - stamDrainRate) // Drain at normal fly rate
            else if (state.gliding.get()) min(1.0f,stam - stamDrainRate * glideMod * diveMod) // Gliding drain logic
            else max(0.0f,stam - stamDrainRate * 0.5f) // if hovering half the stam drain

        state.stamina.set(newStam)
    }

    override fun rotation(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {
        return Vec2.ZERO//Vec2(driver.xRot * 0.5f, driver.yRot)
    }

    override fun velocity(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        val controller = (vehicle as? OrientationControllable)?.orientationController
        if (controller == null || controller.orientation == null) return Vec3.ZERO

        //Perform ride velocity update
        calculateRideSpaceVel(settings, state, vehicle, driver)

        // The downward force used to encourage players to stop flying upside down.
        val extraDownwardForce = state.currUpsideDownForce.get().pow(2) * -0.3 // 6 blocks a second downward

        // Convert the local velocity vector into a world vector
        val localVelVec = Vector3f(
            state.rideVelocity.get().x.toFloat(),
            (state.rideVelocity.get().y).toFloat(),
            state.rideVelocity.get().z.toFloat() * -1.0f // Flip the z axis to make this left handed
        )
        // Unrotate preemptively as this vector gets rotatee later down the line in MC logic.
        var worldVelVec = localVelVec.mul(controller.orientation).toVec3d().yRot(vehicle.yRot.toRadians())
        worldVelVec =  worldVelVec.add(0.0, extraDownwardForce, 0.0) // Add the stamina depletion force to bring the ride down.

        return worldVelVec
    }

    /*
    *  Calculates the change in the ride space vector due to player input and ride state
    */
    fun calculateRideSpaceVel(settings: BirdSettings, state: BirdState, vehicle: PokemonEntity, driver: Player) {
        // retrieve stats
        val topSpeed = (vehicle.runtime.resolveDouble(settings.speedExpr ?: globalBird.speedExpr!!) / 20.0)
        val accel = topSpeed / (vehicle.runtime.resolveDouble(settings.accelerationExpr ?: globalBird.accelerationExpr!!) * 20.0)
        val glideTopSpeed = topSpeed * vehicle.runtime.resolveDouble(settings.glideSpeedExpr ?: globalBird.glideSpeedExpr!!)
        var glideSpeedChange = 0.0
        var activeInput = false

        var newVelocity = Vec3(state.rideVelocity.get().x, state.rideVelocity.get().y, state.rideVelocity.get().z)

        // Account for collision
        if (vehicle.verticalCollision || vehicle.horizontalCollision) {
            newVelocity = newVelocity.normalize().scale(vehicle.deltaMovement.length())
        }

        // speed up and slow down based on input
        if (driver.zza != 0.0f && state.stamina.get() > 0.0 && newVelocity.length() < glideTopSpeed) {
            //make sure it can't exceed top speed
            val forwardInput = when {
                state.stamina.get() == 0.0f -> 0.0
                driver.zza > 0 && newVelocity.z > topSpeed -> 0.0
                driver.zza < 0 && newVelocity.z < (-topSpeed / 3.0) -> 0.0
                else -> driver.zza.sign
            }

            newVelocity = Vec3(
                newVelocity.x,
                newVelocity.y,
                (newVelocity.z + (accel * forwardInput.toDouble())))

            activeInput = true
        }

        val controller = (vehicle as? OrientationControllable)?.orientationController
        if (controller != null) {
            //Base glide speed change on current pitch of the ride.
            glideSpeedChange = sin(Math.toRadians(controller.pitch.toDouble()))
            glideSpeedChange = glideSpeedChange * 0.25

            if (glideSpeedChange <= 0.0) {
                //Ensures that a propelling force is still able to be applied when
                //climbing in height
                if (driver.zza <= 0) {
                    //speed decrease should be 2x speed increase?
                    //state.currSpeed = max(state.currSpeed + (0.0166) * glideSpeedChange, 0.0 )
                    newVelocity = Vec3(
                        newVelocity.x,
                        newVelocity.y,
                        lerp( newVelocity.z, 0.0,glideSpeedChange * -0.0166 * 2 )
                    )
                }
            } else {
                // only add to the speed if it hasn't exceeded the current
                //glide angles maximum amount of speed that it can give.
                //state.currSpeed = min(state.currSpeed + ((0.0166 * 2) * glideSpeedChange), maxGlideSpeed)
                newVelocity = Vec3(
                    newVelocity.x,
                    newVelocity.y,
                    min(newVelocity.z + ((0.0166 * 2) * glideSpeedChange), glideTopSpeed)
                )
            }
        }

        //Vertical movement based on driver input.
        val vertTopSpeed = topSpeed / 2.0
        val vertInput = when {
            state.stamina.get() == 0.0f -> 0.0
            driver.jumping && state.rideVelocity.get().length() < 0.3 -> 1.0
            driver.isShiftKeyDown && state.rideVelocity.get().length() < 0.3  -> -1.0
            else -> 0.0
        }

        if (vertInput != 0.0 && state.stamina.get() > 0.0) {
            newVelocity = Vec3(
                newVelocity.x,
                (newVelocity.y + (accel * vertInput)).coerceIn(-vertTopSpeed, vertTopSpeed),
                newVelocity.z)
            activeInput = true
        }
        else {
            newVelocity = Vec3(
                newVelocity.x,
                lerp(newVelocity.y, 0.0, vertTopSpeed / 20.0),
                newVelocity.z)
        }

        //Check if the ride should be gliding
        if (driver.isLocalPlayer) {
            if ((activeInput && state.stamina.get() > 0.0) || state.rideVelocity.get().z < 0.2) {
                state.gliding.set(false)
            } else {
                state.gliding.set(true)
            }
        }

        //air resistance
        if( abs(newVelocity.z) > 0) {
            newVelocity = newVelocity.subtract(0.0, 0.0, min(0.0015 * newVelocity.z.sign, newVelocity.z))
        }
        state.rideVelocity.set(newVelocity)
    }

    override fun angRollVel(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        if (vehicle !is OrientationControllable) return Vec3.ZERO
        val controller = (vehicle as OrientationControllable).orientationController

        val handling = vehicle.runtime.resolveDouble(settings.handlingExpr ?: globalBird.handlingExpr!!)

        val yawDeltaDeg =  deltaTime * handling * sin(Math.toRadians(controller.roll.toDouble()))
        val trueYawDelt = yawDeltaDeg * abs(cos(Math.toRadians(controller.pitch.toDouble()))).coerceIn(-1.0, 1.0)

        // Dampen yaw when upside down
        val yawDampen = (1 - abs(min(cos(controller.roll.toRadians()),0.0f)))
        controller.applyGlobalYaw(trueYawDelt.toFloat() * yawDampen)

        // Correct orientation when at low speeds
        correctOrientation(settings, state, vehicle, controller, deltaTime)

        val pCorr = (state.currPitchCorrectionForce.get() * deltaTime).toFloat()
        val rCorr = (state.currRollCorrectionForce.get() * deltaTime).toFloat()
        controller.applyGlobalPitch(pCorr)
        controller.rotateRoll(rCorr)

        //yaw, pitch, roll
        return Vec3(0.0, 0.0, 0.0)
    }

    private fun correctOrientation(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity,
        controller: OrientationController,
        deltaTime: Double
    ) {
        // Calculate correcting roll force.
        val rollCorrectionTimer = vehicle.runtime.resolveDouble(settings.timeToRollCorrect ?: globalBird.timeToRollCorrect!!)
        // If the ride is moving then have the correction rate be an equal proportion of:
        // - how rolled are you
        // - how long has it been since last mouse input (higher is stronger correction)
        // Also the correction is proportional as a whole to how NOT pitched are you so you don't spin endlessly when looking up or down
        val howRolledAmI =  sqrt(abs((cos(controller.roll.toRadians()) - 1.0f) * 0.5f)) // sqrt so it trends towards one pretty quickly
        val maxRollCorrectionRate =
            if (state.rideVelocity.get().length() < 0.2) 45.0f
            else 45.0f * cos(controller.pitch.toRadians()).pow(2) * (
                         howRolledAmI*1.0f +
                         (state.noInputTimeRoll.get() - rollCorrectionTimer).coerceIn(0.0, 1.0).pow(2).toFloat()*0.5f
                    )
        val maxRollForce = maxRollCorrectionRate / 40.0f
        val rollArrivalDeg = 35.0f
        val desiredRoll = 0.0f

        // Calculate the signed error (how far and in what direction)
        val rollError = Mth.wrapDegrees(desiredRoll.angleDifference(controller.roll))
        // Use the absolute error to     calculate the magnitude of the influence
        val influenceMagnitude = (min(rollArrivalDeg, abs(rollError)) / rollArrivalDeg)
        val desiredRollForce = maxRollCorrectionRate * influenceMagnitude * -rollError.sign
        var steeredRollForce = desiredRollForce - state.currRollCorrectionForce.get()
        // Ensure the 'steering' correction force doesn't exceed our maxForce
        if (abs(steeredRollForce) > maxRollForce) {
            steeredRollForce = steeredRollForce.sign * maxRollForce
        }
        // Apply the 'steered' force (its really not a steering force I need a better name)
        state.currRollCorrectionForce.set(
            state.currRollCorrectionForce.get() + steeredRollForce
        )

        //Calculate correcting pitch force.
        val maxPitchCorrectionRate = if (state.currUpsideDownForce.get() != 0.0) 15.0f * state.currUpsideDownForce.get().toFloat() else 30.0f
        val maxPitchForce = maxPitchCorrectionRate / 40.0f
        val pitchArrivalDeg = 35.0f
        val desiredPitch = if (state.currUpsideDownForce.get() != 0.0) 90.0f else 0.0f

        if((state.rideVelocity.get().length() < 0.1) || state.currUpsideDownForce.get() != 0.0) {
            val pitchErrror = Mth.wrapDegrees(desiredPitch.angleDifference(controller.pitch))
            val arrivalInfluence = (min(pitchArrivalDeg,abs(pitchErrror)) / pitchArrivalDeg)
            val desiredPitchForce = maxPitchCorrectionRate * arrivalInfluence * -pitchErrror.sign
            var steeredPitchForce = desiredPitchForce - state.currPitchCorrectionForce.get()

            // Ensure the 'steering' correction force doesn't exceed our maxForce
            if (abs(steeredPitchForce) > maxPitchForce) {
                steeredPitchForce = steeredPitchForce.sign * maxPitchForce
            }

            // Apply the 'steered' force (its really not a steering force I need a better name)
            state.currPitchCorrectionForce.set(
                state.currPitchCorrectionForce.get() + steeredPitchForce
            )
        } else {
            state.currPitchCorrectionForce.set(
                //reduce rollCorrection gradually
                lerp(state.currPitchCorrectionForce.get(), 0.0, deltaTime * 0.98)
            )
        }
    }

    override fun rotationOnMouseXY(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity,
        driver: Player,
        mouseY: Double,
        mouseX: Double,
        mouseYSmoother: SmoothDouble,
        mouseXSmoother: SmoothDouble,
        sensitivity: Double,
        deltaTime: Double
    ): Vec3 {
        if (driver !is OrientationControllable) return Vec3.ZERO
        val controller = (driver as OrientationControllable).orientationController
        // Begin the roll correction time counter if not enough mouse input
        // is detected
        if (mouseX < 1 && abs(mouseY) < 1) {
            state.noInputTimeRoll.set(state.noInputTimeRoll.get() + deltaTime)
        } else if (cos(controller.roll.toRadians()) < 0.1) {
            state.noInputTimeRoll.set(state.noInputTimeRoll.get() + deltaTime * 0.5)
        }
        else {
            state.noInputTimeRoll.set(0.0)
        }

        if (abs(mouseY) < 1) {
            state.noInputTimePitch.set(state.noInputTimePitch.get() + deltaTime)
        } else {
            state.noInputTimePitch.set(0.0)
        }

        return rollRotation(
            settings,
            state,
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

    fun rollRotation(
        settings: BirdSettings,
        state: BirdState,
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
        val controller = (vehicle as OrientationControllable).orientationController

        val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr ?: globalBird.speedExpr!!) / 20.0
        val mouseInputMult = vehicle.runtime.resolveDouble(settings.mouseInputMult ?: globalBird.mouseInputMult!!)
        // Applied to mouseX to prevent turning upside down when trying to roll hard.
        val rollDegreeDebuff = if(mouseX.sign.toFloat() == controller.roll.sign) 1 - 0.9f*sqrt(abs(sin(controller.roll.toRadians())))*sqrt(abs(cos(controller.pitch))) else 1.0f

        //Smooth out mouse input.
        val smoothingSpeed = 2.0
        val mouseXc = (mouseX).coerceIn(-45.0, 45.0) * mouseInputMult * rollDegreeDebuff
        // When upside down apply a pitching force to nudge the player towards a dive. This reduces the 'weirdness'
        // of being upside down
        val rolledOverPitchCorrection = -8*sqrt(abs(min(cos(controller.roll.toRadians()),0.0f))) * (cos(controller.pitch.toRadians())).pow(2)
        val mouseYc = ((mouseY).coerceIn(-45.0, 45.0) * mouseInputMult) + rolledOverPitchCorrection
        val xInput = mouseXSmoother.getNewDeltaValue(mouseXc * 0.1, deltaTime * smoothingSpeed)
        val yInput = mouseYSmoother.getNewDeltaValue(mouseYc * 0.1, deltaTime * smoothingSpeed)
        val currSpeed = state.rideVelocity.get().length()
        val currHorzSpeed = state.rideVelocity.get().horizontalDistance()
        val hoverSpeed = 0.1
        val pitchDamping = abs(cos(controller.pitch.toRadians())).pow(2) // Helper value used when wanting to discard the influence of another value at steep pitches

        // Yaw locally when at or below hoverSpeed but not when pitched up or rolled over
        val globalYaw = xInput * (1 - sqrt(RidingBehaviour.scaleToRange(currHorzSpeed, hoverSpeed, topSpeed))).coerceIn(0.0, 1.0)
        controller.applyGlobalYaw(globalYaw.toFloat())

        // Pitch locally up or down depending upon a number of factors:
        // - Reduce pitch substantially when rolled and in a steep yaw. This prevents the player from ignoring a slow handling stat
        // - Do not pitch at all when at or below hover speed
        // - pitch faster at higher speeds
        val p = 1.0 - vehicle.runtime.resolveDouble(settings.horizontalPitchExpr ?: globalBird.horizontalPitchExpr!!)
        var localPitch = yInput * (1 - abs(sin(controller.roll.toRadians())).pow(2) * p * hypot(controller.forwardVector.x.toDouble(), controller.forwardVector.z.toDouble()).toFloat())
        localPitch *= if (currSpeed > hoverSpeed || yInput > 0.0) RidingBehaviour.scaleToRange(currSpeed, 0.0, topSpeed).coerceIn(0.2, 1.0)
            else 0.0

        // Roll less when slow and not at all when at hoverSpeed or lower.
        // Apply a roll righting force when trying to pitch hard while rolled sideways. This nudges the player towards
        // pitching globally
        val pitchInfluencedRollCorrection = 0.0
        val howRolledAmI =  (( cos(controller.roll.toRadians()) - 1).coerceIn(-1.0f, 0.0f) * -2) //Coerce into how much roll you want to let in in the deadzone
        val isInputtingTowardsRoll = if ( xInput.sign == controller.roll.sign.toDouble()) 1.0 else 0.0
        val inputInRolledDir = if (isInputtingTowardsRoll == 1.0) 1.0 else -cos(controller.roll.toRadians()).coerceIn(-1.0f, 0.0f).toDouble()
        val rollDampen = (1 - howRolledAmI.coerceIn(0.0f, 0.7f) * pitchDamping * inputInRolledDir)
        val rollForce = if (currHorzSpeed > hoverSpeed) xInput * sqrt(abs(RidingBehaviour.scaleToRange(currHorzSpeed, hoverSpeed, topSpeed)).coerceIn(0.0,1.0)) * rollDampen + pitchInfluencedRollCorrection
            else 0.0

        // When already rolled significantly part of the roll force should turn to local yaw force
        val localYawForce = rollForce * abs(sin(controller.roll.toRadians())).pow(2) * pitchDamping * isInputtingTowardsRoll

        //yaw, pitch, roll
        return Vec3(localYawForce, localPitch, rollForce)
    }


    override fun canJump(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return false
    }

    override fun setRideBar(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return (state.stamina.get() / 1.0f)
    }

    override fun jumpForce(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun gravity(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return 0.0
    }

    override fun rideFovMultiplier(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr ?: globalBird.speedExpr!!) / 20.0
        val currSpeed = state.rideVelocity.get().length()

        val speedOverTopSpeed = max( 0.0, currSpeed - topSpeed)

        val normalizedGlideSpeed = RidingBehaviour.scaleToRange(speedOverTopSpeed, 0.0, 0.3)

        // Only ever want the fov change to be a max of 0.2 and for it to have non linear scaling.
        return 1.0f + normalizedGlideSpeed.pow(2).toFloat() * 0.2f
    }

    override fun useAngVelSmoothing(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun inertia(settings: BirdSettings, state: BirdState, vehicle: PokemonEntity): Double {
        return 0.5
    }

    override fun shouldRoll(settings: BirdSettings, state: BirdState, vehicle: PokemonEntity): Boolean {
        return true
    }

    override fun turnOffOnGround(settings: BirdSettings, state: BirdState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun dismountOnShift(settings: BirdSettings, state: BirdState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotateRiderHead(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun damageOnCollision(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity,
        impactVec: Vec3
    ): Boolean {
        if (!state.gliding.get()) return false
        val impactSpeed = impactVec.horizontalDistance().toFloat() * 10f
        return vehicle.causeFallDamage(impactSpeed, 1f, vehicle.damageSources().flyIntoWall())
    }

    override fun getRideSounds(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity
    ): RideSoundSettingsList {
        return settings.rideSounds
    }

    override fun createDefaultState(settings: BirdSettings) = BirdState()

    override fun asMoLangValue(
        settings: BirdSettings,
        state: BirdState,
        vehicle: PokemonEntity
    ): ObjectValue<RidingBehaviour<BirdSettings, BirdState>> {
        val value = super.asMoLangValue(settings, state, vehicle)
        value.functions.put("gliding") { DoubleValue(state.gliding.get()) }
        value.functions.put("last_glide") { DoubleValue(state.lastGlide.get()) }
        return value
    }
}

class BirdSettings : RidingBehaviourSettings {
    override val key = BirdBehaviour.KEY
    override val stats = mutableMapOf<RidingStat, IntRange>()

    var infiniteAltitude: Expression? = null
        private set

    var infiniteStamina: Expression? = null
        private set

    var timeToRollCorrect: Expression? = null
        private set

    var handlingExpr: Expression? = null
    var horizontalPitchExpr: Expression? = null
    var mouseInputMult: Expression? = null
    var speedExpr: Expression? = null
    // Seconds from stationary to top speed
    var accelerationExpr: Expression? = null
    var staminaExpr: Expression? = null

    var glideSpeedExpr: Expression? = null
        private set

    // debuff to stamina drain (increases stamina drain?) depending on how horizontal you're looking (so just drain more when climbing in altitude)
    // So for the base 1.5: If looking straight up and holding forward you drain at 150 percent the rate you would if
    // you were horizontal.
    var stamDrainHorzMod: Expression? = null
        private set

    // long ass variable name
    // Percent of stamina drain during glide (percent of normal drain rate) So base is 25 percent. (change to 0 if you want no drain or negative if you want regen)
    var stamDrainPercRateGlide: Expression? = null
        private set

    // Percent of stamina drain as a factor of how hard you're diving. So if its zero then at full dive you drain no stam.
    // This stacks with `stamDrainPercRateGlide`
    // This is coerced between 1 and 0 or things would get weird.
    var stamDrainPerRateDive: Expression? = null
        private set
    var rideSounds: RideSoundSettingsList = RideSoundSettingsList()

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeRidingStats(stats)
        rideSounds.encode(buffer)
        buffer.writeNullableExpression(infiniteAltitude)
        buffer.writeNullableExpression(infiniteStamina)
        buffer.writeNullableExpression(glideSpeedExpr)
        buffer.writeNullableExpression(handlingExpr)
        buffer.writeNullableExpression(horizontalPitchExpr)
        buffer.writeNullableExpression(mouseInputMult)
        buffer.writeNullableExpression(speedExpr)
        buffer.writeNullableExpression(accelerationExpr)
        buffer.writeNullableExpression(staminaExpr)
        buffer.writeNullableExpression(stamDrainHorzMod)
        buffer.writeNullableExpression(stamDrainPercRateGlide)
        buffer.writeNullableExpression(stamDrainPerRateDive)
        buffer.writeNullableExpression(timeToRollCorrect)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        stats.putAll(buffer.readRidingStats())
        rideSounds = RideSoundSettingsList.decode(buffer)
        infiniteAltitude = buffer.readNullableExpression()
        infiniteStamina = buffer.readNullableExpression()
        glideSpeedExpr = buffer.readNullableExpression()
        handlingExpr = buffer.readNullableExpression()
        horizontalPitchExpr = buffer.readNullableExpression()
        mouseInputMult = buffer.readNullableExpression()
        speedExpr = buffer.readNullableExpression()
        accelerationExpr = buffer.readNullableExpression()
        staminaExpr = buffer.readNullableExpression()
        stamDrainHorzMod = buffer.readNullableExpression()
        stamDrainPercRateGlide = buffer.readNullableExpression()
        stamDrainPerRateDive = buffer.readNullableExpression()
        timeToRollCorrect = buffer.readNullableExpression()
    }
}

class BirdState : RidingBehaviourState() {
    var gliding = ridingState(false, Side.BOTH)
    var lastGlide = ridingState(-100L, Side.CLIENT)
    var currRollCorrectionForce = ridingState(0.0, Side.CLIENT)
    var currPitchCorrectionForce = ridingState(0.0, Side.CLIENT)
    var noInputTimeRoll = ridingState(0.0, Side.CLIENT)
    var noInputTimePitch = ridingState(0.0, Side.CLIENT)
    var currUpsideDownForce = ridingState(0.0, Side.CLIENT)

    override fun encode(buffer: FriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeBoolean(gliding.get())
    }

    override fun decode(buffer: FriendlyByteBuf) {
        super.decode(buffer)
        gliding.set(buffer.readBoolean(), forced = true)
    }

    override fun reset() {
        super.reset()
        gliding.set(false, forced = true)
        lastGlide.set(-100L, forced = true)
        currRollCorrectionForce.set(0.0, forced = true)
        currPitchCorrectionForce.set(0.0, forced = true)
        noInputTimeRoll.set(0.0, forced = true)
        noInputTimePitch.set(0.0, forced = true)
    }

    override fun toString(): String {
        return "BirdState(rideVelocity=${rideVelocity.get()}, stamina=${stamina.get()}, gliding=${gliding.get()})"
    }

    override fun copy() = BirdState().also {
        it.rideVelocity.set(this.rideVelocity.get(), forced = true)
        it.stamina.set(this.stamina.get(), forced = true)
        it.gliding.set(this.gliding.get(), forced = true)
        it.lastGlide.set(this.lastGlide.get(), forced = true)
        it.currRollCorrectionForce.set(this.currRollCorrectionForce.get(), forced = true)
        it.currPitchCorrectionForce.set(this.currPitchCorrectionForce.get(), forced = true)
        it.noInputTimeRoll.set(this.noInputTimeRoll.get(), forced = true)
        it.noInputTimePitch.set(this.noInputTimePitch.get(), forced = true)
    }

    override fun shouldSync(previous: RidingBehaviourState): Boolean {
        if (previous !is BirdState) return false
        if (previous.gliding.get() != gliding.get()) return true
        return super.shouldSync(previous)
    }
}

/**
 * Calculates the shortest difference between two angles.
 * The result will be in the range [-180, 180].
 * A positive result indicates a clockwise direction from this angle to the other,
 * a negative result indicates a counter-clockwise direction.
 */
fun Float.angleDifference(other: Float): Float {
    var diff = other - this
    while (diff <= -180F) diff += 360F
    while (diff > 180F) diff -= 360F
    return diff
}
