/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour.types.land

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
import com.cobblemon.mod.common.api.riding.behaviour.SidedRidingState
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
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.resolveDouble
import com.cobblemon.mod.common.util.toVec3d
import com.cobblemon.mod.common.util.writeNullableExpression
import com.cobblemon.mod.common.util.writeRidingStats
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import net.minecraft.core.Direction
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes
import org.joml.Matrix3f

class VehicleBehaviour : RidingBehaviour<VehicleSettings, VehicleState> {
    companion object {
        val KEY = cobblemonResource("land/vehicle")
    }

    override val key = KEY
    val globalVehicle: VehicleSettings
        get() = CobblemonRideSettings.vehicle

    override fun getRidingStyle(settings: VehicleSettings, state: VehicleState): RidingStyle {
        return RidingStyle.LAND
    }

    val poseProvider = PoseProvider<VehicleSettings, VehicleState>(PoseType.STAND)
        .with(PoseOption(PoseType.WALK) { _, state, _ ->
            return@PoseOption abs(state.rideVelocity.get().horizontalDistance()) > 0.0
        })

//    val poseProvider = PoseProvider<VehicleSettings, VehicleState>(PoseType.STAND)
//        .with(PoseOption(PoseType.WALK) { _, _, entity -> entity.entityData.get(PokemonEntity.MOVING) })

    override fun isActive(settings: VehicleSettings, state: VehicleState, vehicle: PokemonEntity): Boolean {
        return Shapes.create(vehicle.boundingBox).blockPositionsAsListRounded().any {
            //Need to check other fluids
            if (vehicle.isInWater || vehicle.isUnderWater) {
                return@any false
            }
            //This might not actually work, depending on what the yPos actually is. yPos of the middle of the entity? the feet?
            if (it.y.toDouble() == (vehicle.position().y)) {
                val blockState = vehicle.level().getBlockState(it.below())
                return@any !(!blockState.isAir && blockState.fluidState.isEmpty)
            }
            true
        }
    }

    override fun pose(settings: VehicleSettings, state: VehicleState, vehicle: PokemonEntity): PoseType {
        return poseProvider.select(settings, state, vehicle)
    }

    override fun speed(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {

        if (driver.isShiftKeyDown) {
            state.drifting.set(true)
            if(driver.zza != 0.0f) {
                state.poweredDrifting.set(true)
            } else {
                state.poweredDrifting.set(false)
            }
        } else {
            state.drifting.set(false)
            state.poweredDrifting.set(false)
        }

        inAirCheck(state, vehicle)
        return driver.deltaMovement.length().toFloat()
    }

    fun inAirCheck(
        state: VehicleState,
        vehicle: PokemonEntity,
    ) {
        // Check both vertical movement and if there are blocks below.
        val posBelow = vehicle.blockPosition().below()
        val blockStateBelow = vehicle.level().getBlockState(posBelow)
        val isAirOrLiquid = blockStateBelow.isAir || !blockStateBelow.fluidState.isEmpty

        val canSupportEntity = blockStateBelow.isFaceSturdy(vehicle.level(), posBelow, Direction.UP)
        val standingOnSolid = canSupportEntity && !isAirOrLiquid

        // inAir if not on the ground
        val inAir = !(vehicle.deltaMovement.y == 0.0 || standingOnSolid)
        state.inAir.set(inAir)
    }

    override fun rotation(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {

        var newMomentum = state.turnMomentum.get().toDouble()

        // Grab turningAcceleration and divide 20 twice to get
        val turningAcceleration = (vehicle.runtime.resolveDouble(settings.handlingExpr ?: globalVehicle.handlingExpr!!) * 1.5 / 20.0f) / 20.0f * 4.0
        val turnInput =  (driver.xxa *-1.0f) * turningAcceleration

        val maxTurnMomentum = 80.0f / 20.0f

        // Base boost stats off of normal turning stats
        val driftMaxTurnMomentum = maxTurnMomentum * 3.0f
        var driftTurnInput = turnInput * 0.4f

        if(state.drifting.get() || state.inAir.get()) {
            if( newMomentum < driftMaxTurnMomentum) {
                driftTurnInput = driftTurnInput * (vehicle.deltaMovement.horizontalDistance() / vehicle.runtime.resolveDouble(settings.speedExpr ?: globalVehicle.speedExpr!!))
                newMomentum += driftTurnInput
            }
            newMomentum = lerp(newMomentum, 0.0, 0.03)
            if( abs(newMomentum) < 0.05 ) {newMomentum = 0.0}
        } else {
            if(abs(newMomentum) <= maxTurnMomentum && turnInput != 0.0 ) {
                newMomentum += turnInput
                newMomentum.coerceIn(-maxTurnMomentum.toDouble(), maxTurnMomentum.toDouble())
            } else {
                newMomentum = lerp(newMomentum, 0.0, 0.15)
            }
        }

        state.turnMomentum.set(newMomentum.toFloat())
        driver.yRot += newMomentum.toFloat()
        return Vec2(driver.xRot, vehicle.yRot + newMomentum.toFloat())
    }

    override fun velocity(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        state.rideVelocity.set(calculateRideSpaceVel(settings, state, vehicle, driver))
        return state.rideVelocity.get()
    }


    private fun calculateRideSpaceVel(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity,
        driver: Player
    ): Vec3 {

        // Check to see if the ride should be walking or sprinting
        //val walkSpeed = getWalkSpeed(vehicle)
        val rideTopSpeed = vehicle.runtime.resolveDouble(settings.speedExpr ?: globalVehicle.speedExpr!!) * 0.6 * 2.0
        val topSpeed = if (state.drifting.get()) rideTopSpeed * 0.5 else rideTopSpeed

        val accel = vehicle.runtime.resolveDouble(settings.accelerationExpr ?: globalVehicle.accelerationExpr!!) * 0.3 * 3 * 2.0

        //Flag for determining if player is actively inputting
        var activeInput = false

        var newVelocity = Vec3.ZERO
        if (!state.drifting.get()) {
            newVelocity = vehicle.deltaMovement

            newVelocity = newVelocity.yRot(vehicle.yRot.toRadians())

        } else {
            state
            newVelocity = vehicle.deltaMovement

            // Align the direction of movement with the world coordinate space so as not to modify it through turning
            val yawAligned = Matrix3f().rotateY(vehicle.yRot.toRadians())
            newVelocity = (newVelocity.toVector3f().mul(yawAligned)).toVec3d()
        }

        //speed up and slow down based on input
        if (driver.zza != 0.0f && state.stamina.get() > 0.0 && !state.inAir.get()) {
            //make sure it can't exceed top speed
            val forwardInput = when {
                driver.zza > 0 && newVelocity.z > topSpeed -> 0.0
                driver.zza < 0 && newVelocity.z <= 0.0 -> 0.0
                else -> driver.zza.sign
            }

            newVelocity = Vec3(
                newVelocity.x,
                newVelocity.y,
                (newVelocity.z + (accel * forwardInput.toDouble())))

            activeInput = true
        }

        // Gravity logic
        if (vehicle.onGround()) {
            newVelocity = Vec3(newVelocity.x, 0.0, newVelocity.z)
        } else {
            val gravity = (9.8 / ( 20.0)) * 0.2 * 0.25 * 3.0
            val terminalVel = 2.0

            val fallingForce = gravity -  ( newVelocity.z.sign *gravity *(abs(newVelocity.z) / 2.0))
            newVelocity = Vec3(newVelocity.x, max(newVelocity.y - fallingForce, -terminalVel), newVelocity.z)
        }

        //ground Friction
        var friction = 0.003 * 2.0
        if (state.drifting.get()) {friction *= 0.1}
        if (!state.inAir.get()) {
            newVelocity = newVelocity.subtract(
                min(friction , abs(newVelocity.x)) * newVelocity.x.sign,
                0.0,
                min(friction , abs(newVelocity.z)) * newVelocity.z.sign
                )

            // TEST FOR LATERAL SLIP OR WHATEVER
            //TODO: calc lateral slip and friction better!
            if (!state.drifting.get()) {
                newVelocity = newVelocity.subtract(
                    min(friction * 10, abs(newVelocity.x)) * newVelocity.x.sign,
                    0.0,
                    0.0,
                )
            }
        }

        //TODO: Change this so its tied to a jumping stat and representative of the amount of jumps
        val canJump = vehicle.runtime.resolveBoolean(settings.canJump ?: globalVehicle.canJump!!)
        //Jump the thang!
        if (driver.jumping && vehicle.onGround() && canJump) {
            val jumpForce = 0.5

            newVelocity = newVelocity.add(0.0, jumpForce, 0.0)

        }

        return newVelocity
    }

    override fun angRollVel(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun rotationOnMouseXY(
        settings: VehicleSettings,
        state: VehicleState,
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

        //Might need to add the smoothing here for default.
        return Vec3(0.0, mouseY, mouseX)
    }

    override fun canJump(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return vehicle.runtime.resolveBoolean(settings.canJump ?: globalVehicle.canJump!!)
    }

    override fun setRideBar(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return 0.0f
    }

    override fun jumpForce(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun gravity(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return 0.0
    }

    override fun rideFovMultiplier(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return 1.0f
    }

    override fun useAngVelSmoothing(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun inertia(settings: VehicleSettings, state: VehicleState, vehicle: PokemonEntity): Double {
        return 1.0
    }

    override fun shouldRoll(settings: VehicleSettings, state: VehicleState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun turnOffOnGround(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun dismountOnShift(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotateRiderHead(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity
    ): Boolean {
        return true
    }

    override fun getRideSounds(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity
    ): RideSoundSettingsList {
        return settings.rideSounds
    }

    override fun createDefaultState(settings: VehicleSettings) = VehicleState()
    override fun asMoLangValue(
        settings: VehicleSettings,
        state: VehicleState,
        vehicle: PokemonEntity
    ): ObjectValue<RidingBehaviour<VehicleSettings, VehicleState>> {
        val value = super.asMoLangValue(settings, state, vehicle)
        value.functions.put("drifting") { DoubleValue(state.drifting.get()) }
        value.functions.put("powered_drifting") { DoubleValue(state.poweredDrifting.get()) }
        value.functions.put("in_air") { DoubleValue(state.inAir.get()) }
        return value
    }
}

class VehicleSettings : RidingBehaviourSettings {
    override val key = VehicleBehaviour.KEY
    override val stats = mutableMapOf<RidingStat, IntRange>()

    var infiniteStamina: Expression? = null
        private set

    var canJump: Expression? = null
        private set

    var speed: Expression? = null
        private set

    var driveFactor: Expression? = null
        private set

    var reverseDriveFactor: Expression? = null
        private set

    var rotationSpeed: Expression? = null
        private set

    var lookYawLimit: Expression? = null
        private set

    var speedExpr: Expression? = null
        private set

    // Max accel is a whole 1.0 in 1 second. The conversion in the function below is to convert seconds to ticks
    var accelerationExpr: Expression? = null
        private set

    // Between 30 seconds and 10 seconds at the lowest when at full speed.
    var staminaExpr: Expression? = null
        private set

    //Between a one block jump and a ten block jump
    var jumpExpr: Expression? = null
        private set

    var handlingExpr: Expression? = null
        private set

    var rideSounds: RideSoundSettingsList = RideSoundSettingsList()

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeRidingStats(stats)
        rideSounds.encode(buffer)
        buffer.writeNullableExpression(infiniteStamina)
        buffer.writeNullableExpression(canJump)
        buffer.writeNullableExpression(speed)
        buffer.writeNullableExpression(driveFactor)
        buffer.writeNullableExpression(reverseDriveFactor)
        buffer.writeNullableExpression(rotationSpeed)
        buffer.writeNullableExpression(lookYawLimit)
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
        canJump = buffer.readNullableExpression()
        speed = buffer.readNullableExpression()
        driveFactor = buffer.readNullableExpression()
        reverseDriveFactor = buffer.readNullableExpression()
        rotationSpeed = buffer.readNullableExpression()
        lookYawLimit = buffer.readNullableExpression()
        speedExpr = buffer.readNullableExpression()
        accelerationExpr = buffer.readNullableExpression()
        staminaExpr = buffer.readNullableExpression()
        jumpExpr = buffer.readNullableExpression()
        handlingExpr = buffer.readNullableExpression()
    }
}

class VehicleState : RidingBehaviourState() {
    var currSpeed = ridingState(0.0, Side.BOTH)
    var deltaRotation = ridingState(Vec2.ZERO, Side.BOTH)
    var drifting = ridingState(false, Side.CLIENT)
    var poweredDrifting = ridingState(false, Side.CLIENT)
    val turnMomentum: SidedRidingState<Float> = ridingState(0.0f, Side.CLIENT)
    var inAir = ridingState(false, Side.CLIENT)


    override fun reset() {
        super.reset()
        currSpeed.set(0.0, forced = true)
        deltaRotation.set(Vec2.ZERO, forced = true)
        drifting.set(false, forced = true)
        poweredDrifting.set(false, forced = true)
        turnMomentum.set(0.0f, forced = true)
        inAir.set(false, forced = true)
    }

    override fun encode(buffer: FriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeBoolean(drifting.get())
        buffer.writeBoolean(poweredDrifting.get())
        buffer.writeFloat(turnMomentum.get())
        buffer.writeBoolean(inAir.get())
    }

    override fun decode(buffer: FriendlyByteBuf) {
        super.decode(buffer)
        drifting.set(buffer.readBoolean(), forced = true)
        poweredDrifting.set(buffer.readBoolean(), forced = true)
        turnMomentum.set(buffer.readFloat(), forced = true)
        inAir.set(buffer.readBoolean(), forced = true)
    }

    override fun toString(): String {
        return "VehicleState(currSpeed=${currSpeed.get()}, deltaRotation=${deltaRotation.get()})"
    }

    override fun copy() = VehicleState().also {
        it.rideVelocity.set(rideVelocity.get(), forced = true)
        it.stamina.set(stamina.get(), forced = true)
        it.currSpeed.set(currSpeed.get(), forced = true)
        it.deltaRotation.set(deltaRotation.get(), forced = true)
        it.drifting.set(drifting.get(), forced = true)
        it.poweredDrifting.set(poweredDrifting.get(), forced = true)
        it.turnMomentum.set(turnMomentum.get(), forced = true)
        it.inAir.set(inAir.get(), forced = true)
    }

    override fun shouldSync(previous: RidingBehaviourState): Boolean {
        if (previous !is VehicleState) return false
        if (previous.drifting.get() != drifting.get()) return true
        if (previous.poweredDrifting.get() != poweredDrifting.get()) return true
        if (previous.turnMomentum.get() != turnMomentum.get()) return true
        if (previous.inAir.get() != inAir.get()) return true
        return super.shouldSync(previous)
    }
}
