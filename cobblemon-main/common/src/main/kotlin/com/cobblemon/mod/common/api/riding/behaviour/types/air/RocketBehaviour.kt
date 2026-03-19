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
import com.cobblemon.mod.common.api.molang.ObjectValue
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
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.util.Mth
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.joml.Matrix3f
import kotlin.math.*

class RocketBehaviour : RidingBehaviour<RocketSettings, RocketState> {
    companion object {
        val KEY = cobblemonResource("air/rocket")
    }

    override val key = KEY
    val globalRocket: RocketSettings
        get() = CobblemonRideSettings.rocket

    override fun getRidingStyle(settings: RocketSettings, state: RocketState): RidingStyle {
        return RidingStyle.AIR
    }

    val poseProvider = PoseProvider<RocketSettings, RocketState>(PoseType.HOVER)
        .with(PoseOption(PoseType.FLY) { _, state, _ ->
            return@PoseOption state.boosting.get()
        })

    override fun isActive(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity
    ): Boolean {
        return !((vehicle.isInWater || vehicle.isUnderWater) || vehicle.onGround())
    }

    override fun pose(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity
    ): PoseType {
        return this.poseProvider.select(settings, state, vehicle)
    }

    override fun speed(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        // Use this as a "tick" function and check to see if the driver is "boosting"
        if (vehicle.level().isClientSide) {
            handleBoosting(settings, state, vehicle, driver)
            tickStamina(settings, state, vehicle, driver)
        }

        return state.rideVelocity.get().length().toFloat()
    }

    fun handleBoosting(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        driver: Player
    ) {
        //If the forward key is not held then it cannot be boosting
        if(Minecraft.getInstance().options.keyUp.isDown() && state.stamina.get() != 0.0f) {
            val boostKeyPressed = Minecraft.getInstance().options.keySprint.isDown()
                //If on the previous tick the boost key was held then don't change if the ride is boosting
                if(state.boostIsToggleable.get() && boostKeyPressed) {
                    //flip the boosting state if boost key is pressed
                    state.boosting.set(!state.boosting.get())
                }
                //If the boost key is not held then next tick boosting is toggleable
                state.boostIsToggleable.set(!boostKeyPressed)
        } else {
            //Turn off boost and reset boost params
            state.boostIsToggleable.set(true)
            state.boosting.set(false)
            state.canSpeedBurst.set(true)
        }
    }

    fun tickStamina(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        driver: Player
    ) {
        val stam = state.stamina.get()

        if (vehicle.runtime.resolveBoolean(settings.infiniteStamina ?: globalRocket.infiniteStamina!!)) {
            return
        }

        // Grab the boost time in seconds and convert to ticks. Then calculate the drain rate as inversely
        // proportional to the number of ticks of boost thus making a full boost take x ticks
        // in short: "Stamina drains slower at higher values and also replenishes slower"
        val boostTime = vehicle.runtime.resolveDouble(settings.staminaExpr ?: globalRocket.staminaExpr!!) * 20.0f
        val stamDrainRate = (1.0f / boostTime).toFloat()

        val newStam = if(state.boosting.get() || driver.jumping) max(0.0f,stam - stamDrainRate) else stam

        state.stamina.set(newStam)
    }

    override fun updatePassengerRotation(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ) {
        return
    }

    override fun clampPassengerRotation(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ) {
        val f = Mth.wrapDegrees(driver.yRot - vehicle.yRot)
        val lookYawLimit = 90.0f
        val g = Mth.clamp(f, -lookYawLimit, lookYawLimit)

        val ticksBeforeCorrection = 40

        if(state.prevCamYRot.get() == driver.yRot){
            state.ticksNoInput.set(state.ticksNoInput.get() + 1)
        } else {
            state.ticksNoInput.set(0)
        }
        val correctionForce = if(state.ticksNoInput.get() >= ticksBeforeCorrection && f != 0.0f && state.boosting.get()) 1f * min(sqrt(lookYawLimit / (abs(f))), 10.0f) else 0.0f
        driver.yRotO += g - f
        driver.yRot = driver.yRot + g - f //- ( clamp(f.sign * correctionForce, -abs(f), abs(f)) )
        //driver.yRot = driver.yRot + g - f - ( min )
        state.prevCamYRot.set(driver.yRot)
        driver.setYHeadRot(driver.yRot)
    }


    override fun rotation(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {

        var newMomentum = state.turnMomentum.get().toDouble()
        // Degrees per tick
        val maxTurnMomentum = vehicle.runtime.resolveDouble(settings.maxTurnRate ?: globalRocket.maxTurnRate!!) / 20.0f
        // Turn rate increase per tick. Based off number of seconds to get to max turn rate
        val turningAcceleration = maxTurnMomentum / (vehicle.runtime.resolveDouble(settings.handlingExpr ?: globalRocket.handlingExpr!!) * 20.0f)
        val turnInput =  (driver.xxa *-1.0f) * turningAcceleration

        // Base boost stats off of normal turning stats
        val boostHandlingMod = vehicle.runtime.resolveDouble(settings.boostHandlingMod ?: globalRocket.boostHandlingMod!!)
        val boostMaxTurnMomentum = maxTurnMomentum * boostHandlingMod
        val boostTurnInput = turnInput * boostHandlingMod

        if(state.boosting.get()) {
            if(driver.xxa != 0.0f && abs(newMomentum + turnInput) < (boostMaxTurnMomentum)) { //If max momentum will not be exceeded then modulate
                newMomentum += boostTurnInput
            } else {
                newMomentum = lerp(newMomentum, 0.0, 0.05)
                newMomentum = newMomentum.coerceIn(-boostMaxTurnMomentum, boostMaxTurnMomentum)
            }
        } else {
            if(driver.xxa == 0.0f) { //If no turning input then lerp to 0
                newMomentum = lerp(newMomentum, 0.0, 0.05)
            } else if(abs(newMomentum + turnInput) < maxTurnMomentum) { //If max momentum will not be exceeded then modulate
                newMomentum += turnInput
            }
        }


        state.turnMomentum.set(newMomentum.toFloat())
        driver.yRot += newMomentum.toFloat()
        return Vec2(driver.xRot, vehicle.yRot + newMomentum.toFloat())

    }


    override fun velocity(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        val retVel = calculateRideSpaceVel(settings, state, vehicle, driver)
        state.rideVelocity.set(retVel)
        return retVel
    }

    private fun calculateRideSpaceVel(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        driver: Player
    ): Vec3 {
        val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr ?: globalRocket.speedExpr!!)
        val accel = vehicle.runtime.resolveDouble(settings.accelerationExpr ?: globalRocket.accelerationExpr!!)
        val jump = vehicle.runtime.resolveDouble(settings.jumpExpr ?: globalRocket.jumpExpr!!)

        var newVelocity = vehicle.deltaMovement

        // Align the direction of movement with the world coordinate space so as not to modify it through turning
        val yawAligned = Matrix3f().rotateY(vehicle.yRot.toRadians())
        newVelocity = (newVelocity.toVector3f().mul(yawAligned)).toVec3d()

        /******************************************************
         * Speed up and slow down based on input
         *****************************************************/
        if (driver.zza != 0.0f && !state.boosting.get()) {
            //make sure it can't exceed top speed
            val forwardInput = when {
                vehicle.deltaMovement.horizontalDistance() > topSpeed && (driver.zza.sign == newVelocity.z.sign.toFloat()) -> 0.0
                else -> driver.zza.sign
            }

            newVelocity = Vec3(
                newVelocity.x,
                newVelocity.y,
                (newVelocity.z + (accel * forwardInput.toDouble())))

        } else if ((state.boosting.get())) {
            val boostMod = vehicle.runtime.resolveDouble(settings.boostSpeedMod ?: globalRocket.boostSpeedMod!!)
            val boostSpeed = topSpeed * boostMod
            val boostAccel = if(newVelocity.length() < boostSpeed) accel * (boostMod/1.5) else 0.0
            val forwardInput = 1.0f
            var burst = 0.0f

            if(state.canSpeedBurst.get()) {
                burst = 0.5f
                state.stamina.set(max(0.0f,state.stamina.get() - 0.05f))
                state.canSpeedBurst.set(false)
            }

            newVelocity = Vec3(
                newVelocity.x,
                newVelocity.y,
                newVelocity.z + (boostAccel * forwardInput.toDouble()) + burst)
        }

        //Vertical movement based on driver input.
        val maxVertSpeed = jump
        val boostMaxVertSpeed = 0.5f * 0.25f
        var vertInput = 0.0

        //If boosting then don't lose altitude
        //otherwise propel up or fall
        if (state.boosting.get() && !driver.jumping && vehicle.deltaMovement.y > (-boostMaxVertSpeed)) {
            vertInput = -0.7
            newVelocity = Vec3(
                newVelocity.x,
                newVelocity.y + accel * vertInput,
                newVelocity.z)
        } else if(state.boosting.get() && vehicle.deltaMovement.y < maxVertSpeed) {
            vertInput = when {
                driver.jumping -> 1.0
                else -> 0.0
            }
            newVelocity = Vec3(
                newVelocity.x,
                (newVelocity.y + (accel * vertInput)),
                newVelocity.z)
        } else {
            if (driver.jumping && !(vehicle.deltaMovement.y > maxVertSpeed && (newVelocity.y.sign.toFloat() > 0.0))) {
                // More force if traveling downwards to allow for quicker fall stops
                vertInput = (if (state.stamina.get() == 0.0f) -1.0f else if(newVelocity.y < 0) jump.toFloat() * 3.0f else jump.toFloat()).toDouble()
                // Reset falldistance if upward motion is detected
                vehicle.resetFallDistance()
                newVelocity = Vec3(
                    newVelocity.x,
                    (newVelocity.y + (accel * vertInput)),
                    newVelocity.z)
            } else {
                /******************************************************
                 * Gravity logic
                 *****************************************************/
                val gravity = (9.8 / ( 20.0)) * 0.2 * 0.6
                val terminalVel = 2.0

                val fallingForce = gravity
                newVelocity = Vec3(newVelocity.x, max(newVelocity.y - fallingForce, -terminalVel), newVelocity.z)

            }
        }

        return newVelocity
    }

    override fun angRollVel(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun rotationOnMouseXY(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        driver: Player,
        mouseY: Double,
        mouseX: Double,
        mouseYSmoother: SmoothDouble,
        mouseXSmoother: SmoothDouble,
        sensitivity: Double,
        deltaTime: Double
    ): Vec3 {
        //Smooth out mouse input.
        val smoothingSpeed = 4
        val xInput = mouseXSmoother.getNewDeltaValue(mouseX * 0.1, deltaTime * smoothingSpeed);
        val yInput = mouseYSmoother.getNewDeltaValue(mouseY * 0.1, deltaTime * smoothingSpeed);

        //yaw, pitch, roll
        return Vec3(0.0, yInput, xInput)
    }

    override fun canJump(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return false
    }

    override fun setRideBar(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        //Retrieve stamina from state and use it to set the "stamina bar"
        return (state.stamina.get() / 1.0f)
    }

    override fun jumpForce(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun gravity(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return 0.0
    }

    override fun rideFovMultiplier(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        if (state.boosting.get()) {
            val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr ?: globalRocket.speedExpr!!)
            val boostMod = vehicle.runtime.resolveDouble(settings.boostSpeedMod ?: globalRocket.boostSpeedMod!!)
            val normalizedBoostSpeed = RidingBehaviour.scaleToRange(state.rideVelocity.get().length(), topSpeed, topSpeed * boostMod)
            return 1.0f + normalizedBoostSpeed.pow(2).toFloat() * 0.2f
        } else {
            return 1.0f
        }
    }

    override fun useAngVelSmoothing(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun inertia(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity
    ): Double {
        return 1.0
    }

    override fun shouldRoll(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun turnOffOnGround(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun dismountOnShift(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotateRiderHead(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity
    ): Boolean {
        return true
    }


    override fun damageOnCollision(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity,
        impactVec: Vec3
    ): Boolean {
        if (!state.boosting.get()) return false
        val impactSpeed = impactVec.horizontalDistance().toFloat() * 10f
        return vehicle.causeFallDamage(impactSpeed, 1f, vehicle.damageSources().flyIntoWall())
    }

    override fun getRideSounds(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity
    ): RideSoundSettingsList {
        return settings.rideSounds
    }

    override fun createDefaultState(settings: RocketSettings) = RocketState()

    override fun asMoLangValue(
        settings: RocketSettings,
        state: RocketState,
        vehicle: PokemonEntity
    ): ObjectValue<RidingBehaviour<RocketSettings, RocketState>> {
        val value = super.asMoLangValue(settings, state, vehicle)
        value.functions.put("boosting") { DoubleValue(state.boosting.get()) }
        value.functions.put("can_speed_burst") { DoubleValue(state.canSpeedBurst.get()) }
        return value
    }
}

class RocketSettings : RidingBehaviourSettings {
    override val key = RocketBehaviour.KEY
    override val stats = mutableMapOf<RidingStat, IntRange>()

    var infiniteStamina: Expression? = null
        private set
    // Boost multiplier for speed
    var boostSpeedMod: Expression? = null
        private set

    // Boost multiplier for handling
    var boostHandlingMod: Expression? = null
        private set

    // Max turn rate in degrees per second
    var maxTurnRate: Expression? = null
        private set

    var speedExpr: Expression? = null
        private set

    // Max accel is a whole 1.0 in 1 second. The conversion in the function below is to convert seconds to ticks
    var accelerationExpr: Expression? = null
        private set

    // air time in seconds
    var staminaExpr: Expression? = null
        private set

    var jumpExpr: Expression? = null
        private set

    // How long it takes to get to max turn rate from rest (in seconds)
    var handlingExpr: Expression? = null
        private set

    var rideSounds: RideSoundSettingsList = RideSoundSettingsList()

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeRidingStats(stats)
        rideSounds.encode(buffer)
        buffer.writeNullableExpression(infiniteStamina)
        buffer.writeNullableExpression(boostSpeedMod)
        buffer.writeNullableExpression(boostHandlingMod)
        buffer.writeNullableExpression(maxTurnRate)
        buffer.writeNullableExpression(speedExpr)
        buffer.writeNullableExpression(accelerationExpr)
        buffer.writeNullableExpression(staminaExpr)
        buffer.writeNullableExpression(jumpExpr)
        buffer.writeNullableExpression(handlingExpr)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        stats.putAll(buffer.readRidingStats())
        rideSounds = RideSoundSettingsList.decode(buffer)
        infiniteStamina = buffer.readNullableExpression()
        boostSpeedMod = buffer.readNullableExpression()
        boostHandlingMod = buffer.readNullableExpression()
        maxTurnRate = buffer.readNullableExpression()
        speedExpr = buffer.readNullableExpression()
        accelerationExpr = buffer.readNullableExpression()
        staminaExpr = buffer.readNullableExpression()
        jumpExpr = buffer.readNullableExpression()
        handlingExpr = buffer.readNullableExpression()
    }
}

class RocketState : RidingBehaviourState() {
    var boosting = ridingState(false, Side.BOTH)
    var boostIsToggleable = ridingState(false, Side.BOTH)
    var canSpeedBurst = ridingState(false, Side.BOTH)
    var prevCamYRot: SidedRidingState<Float> = ridingState(0.0f, Side.CLIENT)
    var ticksNoInput: SidedRidingState<Int> = ridingState(0, Side.CLIENT)
    val turnMomentum: SidedRidingState<Float> = ridingState(0.0f, Side.CLIENT)

    override fun reset() {
        super.reset()
        boosting.set(false, forced = true)
        boostIsToggleable.set(false, forced = true)
        canSpeedBurst.set(true, forced = true)
        prevCamYRot.set(0.0f, forced = true)
        ticksNoInput.set(0, forced = true)
        turnMomentum.set(0.0f, forced = true)
    }

    override fun copy() = RocketState().also {
        it.rideVelocity.set(this.rideVelocity.get(), forced = true)
        it.stamina.set(this.stamina.get(), forced = true)
        it.boosting.set(this.boosting.get(), forced = true)
        it.boostIsToggleable.set(this.boosting.get(), forced = true)
        it.canSpeedBurst.set(this.canSpeedBurst.get(), forced = true)
        it.prevCamYRot.set(this.prevCamYRot.get(), forced = true)
        it.ticksNoInput.set(this.ticksNoInput.get(), forced = true)
        it.turnMomentum.set(this.turnMomentum.get(), forced = true)
    }

    override fun encode(buffer: FriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeBoolean(boosting.get())
        buffer.writeBoolean(boostIsToggleable.get())
        buffer.writeBoolean(canSpeedBurst.get())
        buffer.writeFloat(prevCamYRot.get())
        buffer.writeInt(ticksNoInput.get())
        buffer.writeFloat(turnMomentum.get())
    }

    override fun decode(buffer: FriendlyByteBuf) {
        super.decode(buffer)
        boosting.set(buffer.readBoolean(), forced = true)
        boostIsToggleable.set(buffer.readBoolean(), forced = true)
        canSpeedBurst.set(buffer.readBoolean(), forced = true)
        prevCamYRot.set(buffer.readFloat(), forced = true)
        ticksNoInput.set(buffer.readInt(), forced = true)
        turnMomentum.set(buffer.readFloat(), forced = true)
    }

    override fun shouldSync(previous: RidingBehaviourState): Boolean {
        if (previous !is RocketState) return false
        if (previous.boosting.get() != boosting.get()) return true
        if (previous.boostIsToggleable.get() != boostIsToggleable.get()) return true
        if (previous.canSpeedBurst.get() != canSpeedBurst.get()) return true
        if (previous.prevCamYRot.get() != prevCamYRot.get()) return true
        if (previous.ticksNoInput.get() != ticksNoInput.get()) return true
        if (previous.turnMomentum.get() != turnMomentum.get()) return true
        return super.shouldSync(previous)
    }
}
