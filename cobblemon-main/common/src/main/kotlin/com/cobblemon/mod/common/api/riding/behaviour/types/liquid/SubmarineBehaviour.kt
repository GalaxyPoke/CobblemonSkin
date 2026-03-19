/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour.types.liquid

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
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.math.geometry.toRadians
import com.cobblemon.mod.common.util.readNullableExpression
import com.cobblemon.mod.common.util.readRidingStats
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.resolveDouble
import com.cobblemon.mod.common.util.toVec3d
import com.cobblemon.mod.common.util.writeNullableExpression
import com.cobblemon.mod.common.util.writeRidingStats
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.tags.FluidTags
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.*

class SubmarineBehaviour : RidingBehaviour<SubmarineSettings, SubmarineState> {
    companion object {
        val KEY = cobblemonResource("liquid/submarine")
    }

    override val key = KEY
    val globalSubmarine: SubmarineSettings
        get() = CobblemonRideSettings.submarine

    override fun getRidingStyle(settings: SubmarineSettings, state: SubmarineState): RidingStyle {
        return RidingStyle.LIQUID
    }

    val poseProvider = PoseProvider<SubmarineSettings, SubmarineState>(PoseType.FLOAT)
        .with(PoseOption(PoseType.SWIM) { _, state, entity -> state.rideVelocity.get().z > 0.05 && !state.onSurface.get()})
        .with(PoseOption(PoseType.STAND) { _, state, entity -> state.rideVelocity.get().z <= 0.05 && state.onSurface.get()})
        .with(PoseOption(PoseType.SWIM) { _, state, entity -> state.rideVelocity.get().z > 0.05 && state.onSurface.get()})

    override fun isActive(settings: SubmarineSettings, state: SubmarineState, vehicle: PokemonEntity): Boolean {
        return vehicle.isInWater || !vehicle.onGround()
    }

    override fun pose(settings: SubmarineSettings, state: SubmarineState, vehicle: PokemonEntity): PoseType {
        return poseProvider.select(settings, state, vehicle)
    }

    override fun speed(settings: SubmarineSettings, state: SubmarineState, vehicle: PokemonEntity, driver: Player): Float {
        return state.speed.get().toFloat()
    }

    override fun tick(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ) {
        if(vehicle.level().isClientSide) {
            tickStamina(settings, state, vehicle)
            surfaceCheck(settings, state, vehicle, driver)
        }
    }

    fun tickStamina(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity
    ) {
        val stam = state.stamina.get()

        if (vehicle.runtime.resolveBoolean(settings.infiniteStamina ?: globalSubmarine.infiniteStamina!!)) {
            return
        }

        // Grab the boost time in seconds and convert to ticks. Then calculate the drain rate as inversely
        // proportional to the number of ticks of boost thus making a full boost take x ticks
        // in short: "Stamina drains slower at higher values and also replenishes slower"
        val breathTime = vehicle.runtime.resolveDouble(settings.staminaExpr ?: globalSubmarine.staminaExpr!!) * 20.0f
        val stamRegenRate = 1.0f / (vehicle.runtime.resolveDouble(settings.staminaRegenTime ?: globalSubmarine.staminaRegenTime!!).toFloat() * 20.0f)
        val stamDrainRate = (1.0f / breathTime).toFloat()
        val driver = vehicle.controllingPassenger
        val newStam = if (driver?.isEyeInFluid(FluidTags.WATER) ?: false) max(0.0f,stam - stamDrainRate)
            else min(1.0f,stam + stamRegenRate)

        state.stamina.set(newStam)
    }

    private fun surfaceCheck(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity,
        driver: Player,
    ) {
        // determine if the vehicle is in water.
        // if it isn't then set surface flag to false and return
        if (!vehicle.isInWater) {
            state.onSurface.set(false)
            return
        }

        val hitboxBreachPercentage = vehicle.runtime.resolveDouble(settings.hitboxBreachPercentage ?: globalSubmarine.hitboxBreachPercentage!!)

        // Grab the position of the vehicle that when out of the water is considered breached
        val vehicleBreachPoint = vehicle.position().add(0.0, vehicle.bbHeight * hitboxBreachPercentage, 0.0)

        // If the block above the center is not fluid but this one is then move the center
        val centerBlockPos = BlockPos.containing(vehicleBreachPoint)
        val centerInWater = !vehicle.level().getFluidState(centerBlockPos).isEmpty

        val abovePos = centerBlockPos.above()
        val aboveInWater = !vehicle.level().getFluidState(abovePos).isEmpty

        if (!centerInWater && !aboveInWater) {
            state.onSurface.set(true)
        } else {
            state.onSurface.set(false)
        }

        //

        // Function for if x mouse and y mouse "get through" to affecting the mounts rotation. Return a Pair<Boolean, Boolean>
    }

    override fun rotation(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {
        return Vec2(driver.xRot * 0.5f, driver.yRot)
    }

    override fun velocity(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        if (vehicle !is OrientationControllable) return Vec3.ZERO
        val controller = (vehicle as OrientationControllable).orientationController
        val orientation = controller.orientation ?: return Vec3.ZERO

        val runtime = vehicle.runtime
        val topSpeed = (vehicle.runtime.resolveDouble(settings.speedExpr ?: globalSubmarine.speedExpr!!) / 20.0)
        val accel = (topSpeed / (vehicle.runtime.resolveDouble(settings.accelerationExpr ?: globalSubmarine.accelerationExpr!!) * 20.0))
        val strafeSpeed = (runtime.resolveDouble(settings.jumpExpr ?: globalSubmarine.jumpExpr!!) / 20.0)
        val strafeAccel = (strafeSpeed / (vehicle.runtime.resolveDouble(settings.accelerationExpr ?: globalSubmarine.accelerationExpr!!) * 20.0))
        var currVel = state.rideVelocity.get()

        /*********************************************
         * Handle collisions
         *********************************************/
        if (vehicle.horizontalCollision || vehicle.verticalCollision) {
            // not actually friction
            val frictionLimit = 0.1
            val postColSpeed = min(max(state.rideVelocity.get().length(), frictionLimit), state.speed.get())
            state.speed.set(postColSpeed)
        }
        if (vehicle.verticalCollision) {
            state.rideVelocity.set(Vec3(state.rideVelocity.get().x, 0.0, state.rideVelocity.get().z))
        }

        /*********************************************
         * Calculate Forward Velocity
         *********************************************/
        val currZVel = state.rideVelocity.get().z
        var newZVel = when {
            driver.zza > 0.0 -> min(topSpeed, currZVel + accel)
            driver.zza < 0.0 -> max( -strafeSpeed, currZVel - strafeAccel)
            else -> lerp(currZVel, 0.0, 0.02)
        }

        // If trying to brake then add the friction to the braking force
        if (driver.zza != 0.0f && driver.zza.sign.toDouble() != newZVel.sign) {
            newZVel = lerp(newZVel, 0.0, 0.02)
        }

        currVel = Vec3(currVel.x, currVel.y, newZVel)

        /*********************************************
         * Calculate up/down Velocity and gravity
         *********************************************/
        val vertTopSpeed = strafeSpeed
        val currYVel = state.rideVelocity.get().y
        val gravity = (9.8 / ( 20.0)) * 0.2 * 0.4
        val terminalYVel = 2.0
        val driverYInput = if(driver.jumping) 1.0 else if (driver.isShiftKeyDown) -1.0 else 0.0
        var newYVel = when {
            (!vehicle.isInWater) -> max(-terminalYVel, currYVel - gravity)
            //(vehicle.isInWater && !vehicle.isUnderWater && currYVel > 0.001) -> lerp(currYVel, 0.0, 0.1) // Make sure that yVel is above a threshold before doing this otherwise it'll block submerging.
            //driverYInput != 0.0 && currYVel > 0.001 && driverYInput.sign != currYVel.sign -> lerp(currYVel, 0.0, 0.05)
            driverYInput > 0.0 -> min(vertTopSpeed, currYVel + strafeAccel)
            driverYInput < 0.0 -> max(-vertTopSpeed, currYVel - strafeAccel)
            else -> lerp(currYVel, 0.0, 0.02)
        }

        // If trying to brake then add the friction to the braking force
        if (driverYInput != 0.0 && driverYInput != newYVel.sign) {
            newYVel = lerp(newYVel, 0.0, 0.02)
        }

        currVel = Vec3(currVel.x, newYVel, currVel.z)

        /*********************************************
         * Calculate left/right strafing Velocity
         *********************************************/
        val currXVel = state.rideVelocity.get().x
        val horzTopSpeed = strafeSpeed
        var newXVel = when {
            driver.xxa > 0.0 -> min(horzTopSpeed, currXVel + strafeAccel)
            driver.xxa < 0.0 -> max( -horzTopSpeed, currXVel - strafeAccel)
            else -> lerp(currXVel, 0.0, 0.02)
        }

        // If trying to brake then add the friction to the braking force
        if (driver.xxa != 0.0f && driver.xxa.sign.toDouble() != newZVel.sign) {
            newXVel = lerp(newXVel, 0.0, 0.02)
        }

        currVel = Vec3(newXVel, currVel.y, currVel.z)

        // Set the new velocity
        state.rideVelocity.set(currVel)

        /*********************************************
         * Separate and apply  velocities
         *********************************************/
        // If the driver and vehicles eyes are out of the water but the vehicle is still in it then move only horizontally or downwards as you have surfaced
        if (state.onSurface.get()) {
            val worldVelVec = Vec3(currVel.x, min(0.0, currVel.y), currVel.z)
            return worldVelVec
        } else {
            val vertVec = Vec3(0.0, currVel.y, 0.0)
            val horzVec = Vec3(currVel.x, 0.0, 0.0)
            val localZVelVec = Vector3f(0.0f, 0.0f, currVel.z.toFloat() * -1.0f) // Flip the z axis to make this left handed
            // Unrotate preemptively as this vector gets rotated later down the line in MC logic.
            val worldVelVec = localZVelVec.mul(orientation).toVec3d().yRot(vehicle.yRot.toRadians())
            // Add back the horizontal and vertical components.
            return worldVelVec.add(vertVec).add(horzVec)
        }
    }

    override fun angRollVel(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun applyRenderRotation(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity,
        partialTicks: Float
    ) {
        if (vehicle !is OrientationControllable) return
        val controller = (vehicle as OrientationControllable).orientationController

        var handling = vehicle.runtime.resolveDouble(settings.handlingExpr ?: globalSubmarine.handlingExpr!!)
        val surfaceHandlingBoost = if(state.onSurface.get()) 3.0 else 1.0
        handling *= surfaceHandlingBoost

        val strafeSpeed = (vehicle.runtime.resolveDouble(settings.jumpExpr ?: globalSubmarine.jumpExpr!!) / 20.0)

        // Grab the roll and see if its hit the limit yet. If it hasn't then trend it towards its desired roll
        val maxRollDeg = if (state.onSurface.get()) 5 else 15
        val maxPitchDeg = 15
        var rollAmount = 0.0
        var pitchAmount = 0.0

        // Do horizontal speed based rolling
        val maxHorzSpeed = strafeSpeed
        val currHorzSpeed = state.rideVelocity.get().x
        var desiredRoll = -(currHorzSpeed / maxHorzSpeed).coerceIn(-1.0, 1.0) * maxRollDeg
        rollAmount += lerp(controller.roll.toDouble(), desiredRoll, partialTicks.toDouble()) - controller.roll
        //val rotAmount   //Maybe use some lerp?
        controller.rotateRoll(rollAmount.toFloat())

        // Do yawRate based rolling
        val maxYawRate = handling
        val currYawRate = vehicle.ridingAnimationData.rotDeltaSpring.getInterpolated(partialTicks.toDouble(), 2).y()
        desiredRoll = -(currYawRate / maxYawRate).coerceIn(-1.0, 1.0) * maxRollDeg
        rollAmount += desiredRoll - controller.roll
        controller.rotateRoll(rollAmount.toFloat())

        //TODO: why does this affect the actual controller but rolling doesn't??
        // Do upwards movement based pitching
//        val maxVertSpeed = 0.25 //TODO: CONNECT UP TO JUMP
//        val currVertSpeed = state.rideVelocity.get().y
//        var desiredPitch = -(currVertSpeed / maxVertSpeed) * maxPitchDeg
//        pitchAmount += lerp(controller.pitch.toDouble(), desiredPitch, partialTicks.toDouble()) - controller.pitch
//        controller.rotatePitch(pitchAmount.toFloat())

    }

    override fun rotationOnMouseXY(
        settings: SubmarineSettings,
        state: SubmarineState,
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

        // Set roll to zero if transitioning to noroll config
        controller.rotateRoll(controller.roll * -1.0f)

        val handling = vehicle.runtime.resolveDouble(settings.handlingExpr ?: globalSubmarine.handlingExpr!!)
        val surfaceHandlingBoost = if(state.onSurface.get()) 3.0 else 1.0
        val effHandling =  if(state.onSurface.get()) 1000 * deltaTime else handling * deltaTime
        //Smooth out mouse input.
        val smoothingSpeed = if(state.onSurface.get()) 10.0 else 2.0
        val mouseXc = (mouseX * 0.1).coerceIn(-effHandling, effHandling)
        val mouseYc = (mouseY * 0.1).coerceIn(-effHandling, effHandling)
        val xInput = mouseXSmoother.getNewDeltaValue(mouseXc, deltaTime * smoothingSpeed);
        val yInput = mouseYSmoother.getNewDeltaValue(mouseYc, deltaTime * smoothingSpeed);

        // Give the ability to yaw with x mouse input when at low speeds.
        val yawForce =  xInput

        // Apply yaw globally as we don't want roll or pitch changes due to local yaw when looking up or down.
        controller.applyGlobalYaw(yawForce.toFloat())

        var pitchRot = yInput
        // Pitch up globally
        val pitchLimit = 45.0
        if (abs(controller.pitch + pitchRot) >= pitchLimit ) {
            pitchRot = 0.0
            mouseYSmoother.reset()
        } else {
            controller.applyGlobalPitch(pitchRot.toFloat())
        }

        // If on the surface then conditionally bring the pitch to 0 degrees.
        if (state.onSurface.get()) {
            //move back at max 30 degrees per second.
            val maxCorrectionSpeed = 30.0
            val arrivalDegreeWidth = 30.0
            val currentPitch = controller.pitch
            val correctionDegrees = (-currentPitch / arrivalDegreeWidth).coerceIn(-1.0, 1.0) * maxCorrectionSpeed * deltaTime

            controller.applyGlobalPitch(correctionDegrees.toFloat())

            // Compensate player ride rotations
//            val playerRotater = driver as RideRotation
//            playerRotater.`cobblemon$setRideXRot`(playerRotater.`cobblemon$getRideXRot`() - correctionDegrees.toFloat() )
        }

        //yaw, pitch, roll
        return Vec3.ZERO
    }

    override fun canJump(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return false
    }

    override fun setRideBar(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return (state.stamina.get() / 1.0f)
    }

    override fun jumpForce(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun gravity(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return 0.0
    }

    override fun rideFovMultiplier(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return 1.0f
    }

    override fun useAngVelSmoothing(settings: SubmarineSettings, state: SubmarineState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun inertia(settings: SubmarineSettings, state: SubmarineState, vehicle: PokemonEntity): Double {
        return if (!vehicle.isUnderWater) 1.0 else 0.1
    }

    override fun shouldRoll(settings: SubmarineSettings, state: SubmarineState, vehicle: PokemonEntity): Boolean {
        return true
    }

    override fun turnOffOnGround(settings: SubmarineSettings, state: SubmarineState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun dismountOnShift(settings: SubmarineSettings, state: SubmarineState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun mouseModifiesDriverRotation(settings: SubmarineSettings, state: SubmarineState, vehicle: PokemonEntity): Pair<Boolean, Boolean> {
        return Pair(false, state.onSurface.get())
    }

    override fun shouldRotatePokemonHead(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotateRiderHead(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun getRideSounds(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity
    ): RideSoundSettingsList {
        return settings.rideSounds
    }

    override fun createDefaultState(settings: SubmarineSettings) = SubmarineState()

    override fun asMoLangValue(
        settings: SubmarineSettings,
        state: SubmarineState,
        vehicle: PokemonEntity
    ): ObjectValue<RidingBehaviour<SubmarineSettings, SubmarineState>> {
        val value = super.asMoLangValue(settings, state, vehicle)
        value.functions.put("on_surface") { DoubleValue(state.onSurface.get()) }
        return value
    }
}

class SubmarineSettings : RidingBehaviourSettings {
    override val key = SubmarineBehaviour.KEY
    override val stats = mutableMapOf<RidingStat, IntRange>()

    var infiniteStamina: Expression? = null
        private set

    var staminaRegenTime: Expression? = null
        private set

    // Percentage (0.0->1.0) of ride needing to be breached out of the water to be considered surfaced.
    var hitboxBreachPercentage: Expression? = null
        private set

    // Tope strafe speed in Bl/s
    var jumpExpr: Expression? = null
    // Yaw rate in degrees per second
    var handlingExpr: Expression? = null
    // Top speed in Bl/s
    var speedExpr: Expression? = null
    // Seconds to get to top speed
    var accelerationExpr: Expression? = null
    // Seconds of air provided by mount
    var staminaExpr: Expression? = null

    var rideSounds: RideSoundSettingsList = RideSoundSettingsList()

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeRidingStats(stats)
        rideSounds.encode(buffer)
        buffer.writeNullableExpression(infiniteStamina)
        buffer.writeNullableExpression(staminaRegenTime)
        buffer.writeNullableExpression(hitboxBreachPercentage)
        buffer.writeNullableExpression(jumpExpr)
        buffer.writeNullableExpression(handlingExpr)
        buffer.writeNullableExpression(speedExpr)
        buffer.writeNullableExpression(accelerationExpr)
        buffer.writeNullableExpression(staminaExpr)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        stats.putAll(buffer.readRidingStats())
        rideSounds = RideSoundSettingsList.decode(buffer)
        infiniteStamina = buffer.readNullableExpression()
        staminaRegenTime = buffer.readNullableExpression()
        hitboxBreachPercentage = buffer.readNullableExpression()
        jumpExpr = buffer.readNullableExpression()
        handlingExpr = buffer.readNullableExpression()
        speedExpr = buffer.readNullableExpression()
        accelerationExpr = buffer.readNullableExpression()
        staminaExpr = buffer.readNullableExpression()
    }
}

class SubmarineState : RidingBehaviourState() {
    var speed = ridingState(0.0, Side.CLIENT)
    var currRollCorrectionForce = ridingState(0.0, Side.CLIENT)
    var currPitchCorrectionForce = ridingState(0.0, Side.CLIENT)
    var noInputTime = ridingState(0.0, Side.CLIENT)
    var onSurface = ridingState(false, Side.BOTH)

    override fun encode(buffer: FriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeBoolean(onSurface.get())
    }

    override fun decode(buffer: FriendlyByteBuf) {
        super.decode(buffer)
        onSurface.set(buffer.readBoolean(), forced = true)
    }

    override fun reset() {
        super.reset()
        speed.set(0.0, forced = true)
        currRollCorrectionForce.set(0.0, forced = true)
        currPitchCorrectionForce.set(0.0, forced = true)
        noInputTime.set(0.0, forced = true)
        onSurface.set(false, forced = true)
    }

    override fun copy() = SubmarineState().also {
        it.stamina.set(stamina.get(), forced = true)
        it.speed.set(speed.get(), forced = true)
        it.rideVelocity.set(rideVelocity.get(), forced = true)
        it.currRollCorrectionForce.set(this.currRollCorrectionForce.get(), forced = true)
        it.currPitchCorrectionForce.set(this.currPitchCorrectionForce.get(), forced = true)
        it.noInputTime.set(this.noInputTime.get(), forced = true)
        it.onSurface.set(this.onSurface.get(), forced = true)
    }

    override fun shouldSync(previous: RidingBehaviourState): Boolean {
        if (previous !is SubmarineState) return false
        if (previous.onSurface.get() != onSurface.get()) return true
        return super.shouldSync(previous)
    }
}
