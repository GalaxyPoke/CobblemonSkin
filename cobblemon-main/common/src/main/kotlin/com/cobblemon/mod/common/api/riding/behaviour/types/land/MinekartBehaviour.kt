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
import com.cobblemon.mod.common.util.resolveFloat
import com.cobblemon.mod.common.util.writeNullableExpression
import com.cobblemon.mod.common.util.writeRidingStats
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
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

class MinekartBehaviour : RidingBehaviour<MinekartSettings, MinekartState> {
    companion object {
        val KEY = cobblemonResource("land/minekart")
    }

    override val key = KEY
    val globalMinekart: MinekartSettings
        get() = CobblemonRideSettings.minekart

    override fun getRidingStyle(settings: MinekartSettings, state: MinekartState): RidingStyle {
        return RidingStyle.LAND
    }

    val poseProvider = PoseProvider<MinekartSettings, MinekartState>(PoseType.STAND)
        .with(PoseOption(PoseType.WALK) { _, state, _ ->
            return@PoseOption abs(state.rideVelocity.get().horizontalDistance()) > 0.0
        })

//    val poseProvider = PoseProvider<MinekartSettings, MinekartState>(PoseType.STAND)
//        .with(PoseOption(PoseType.WALK) { _, _, entity -> entity.entityData.get(PokemonEntity.MOVING) })

    override fun isActive(settings: MinekartSettings, state: MinekartState, vehicle: PokemonEntity): Boolean {
        return Shapes.create(vehicle.boundingBox).blockPositionsAsListRounded().any {
            //Need to check other fluids
            if (vehicle.isInWater || vehicle.isUnderWater) {
                return@any false
            }
            //This might not actually work, depending on what the yPos actually is. yPos of the middle of the entity? the feet?
            if (it.y.toDouble() == (vehicle.position().y)) {
                val blockState = vehicle.level().getBlockState(it.below())
                return@any (!blockState.isAir && blockState.fluidState.isEmpty)
            }
            true
        }
    }

    override fun pose(settings: MinekartSettings, state: MinekartState, vehicle: PokemonEntity): PoseType {
        return poseProvider.select(settings, state, vehicle)
    }

    override fun speed(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {

        val boostMax = vehicle.runtime.resolveFloat(settings.boostLimit ?: globalMinekart.boostLimit!!)
        //TODO: should this be by how much turn has accumulated or simply time turning... or both?
        val boostPerSecond = 20.0f
        val boostLoss = 40.0f

        if (driver.jumping && driver.zza > 0.0f) {
            if (!state.drifting.get()) {
                if (driver.xxa != 0.0f) {
                    state.clockwiseDrift.set(driver.xxa.sign > 0.0)
                    state.turnMomentum.set(0.0f)
                    state.drifting.set(true)
                    state.boost.set(0.0f)
                } else {
                    //TODO: Add jumping logic
                }
            } else {
                state.boost.set((state.boost.get() + boostPerSecond / 20.0f).coerceIn(0.0f, boostMax))
            }
        } else {
            if(driver.zza <= 0.0 && state.boost.get() != 0.0f){
                state.boost.set(0.0f)
            } else if( state.boost.get() > 0.0f ) {
                state.boost.set(max(state.boost.get() - boostLoss, 0.0f))
            }
            state.drifting.set(false)
        }

        inAirCheck(state, vehicle)
        return driver.deltaMovement.length().toFloat()
    }

    fun inAirCheck(
        state: MinekartState,
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
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {

        var newMomentum = state.turnMomentum.get().toDouble()

        //TODO: maybe tie this to max turnspeeds rather than the turn acceleration
        val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr ?: globalMinekart.speedExpr!!)
        val turningAcceleration = (vehicle.runtime.resolveDouble(settings.handlingExpr ?: globalMinekart.handlingExpr!!) * 1.5 / 20.0f) / 20.0f * 4.0
        val turnInput =  (driver.xxa *-1.0f) * turningAcceleration
        val driftInput = turnInput * 0.25f

        val lowSpeedTurnBoost = (RidingBehaviour.scaleToRange(vehicle.deltaMovement.horizontalDistance(), 0.0, topSpeed ).pow(0.5) - 2.0f) * -1.0f
        val forcedDriftTurnMomentum = (80.0f / 20.0f) * lowSpeedTurnBoost
        val maxTurnMomentum = (60.0f / 20.0f) * lowSpeedTurnBoost
        val maxDriftMomentum = forcedDriftTurnMomentum * 0.6f

        if (!state.drifting.get()){
            // perform non drift turning
            if(abs(newMomentum) <= maxTurnMomentum && turnInput != 0.0 ) {
                newMomentum += turnInput
                newMomentum = min(max(newMomentum, maxTurnMomentum * -1.0), maxTurnMomentum.toDouble())
            } else {
                newMomentum = lerp(newMomentum, 0.0, 0.15)
            }

            state.turnMomentum.set(newMomentum.toFloat())
            driver.yRot += newMomentum.toFloat()
            return Vec2(driver.xRot, vehicle.yRot + newMomentum.toFloat())
        } else {
            // perform drift turning
            if(abs(newMomentum) <= maxDriftMomentum && driftInput != 0.0 ) {
                newMomentum += driftInput
                newMomentum = min(max(newMomentum, maxDriftMomentum * -1.0), maxDriftMomentum.toDouble())
            } else {
                      newMomentum = lerp(newMomentum, 0.0, 0.15)
            }

            val signedDriftTurn = if(state.clockwiseDrift.get()) forcedDriftTurnMomentum * -1.0f else forcedDriftTurnMomentum
            state.turnMomentum.set(newMomentum.toFloat())
            driver.yRot += newMomentum.toFloat() + signedDriftTurn.toFloat()
            return Vec2(driver.xRot, vehicle.yRot + newMomentum.toFloat() + signedDriftTurn.toFloat())
        }


    }

    override fun velocity(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        state.rideVelocity.set(calculateRideSpaceVel(settings, state, vehicle, driver))
        return state.rideVelocity.get()
    }


    private fun calculateRideSpaceVel(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity,
        driver: Player
    ): Vec3 {

        // Check to see if the ride should be walking or sprinting
        val rideTopSpeed = vehicle.runtime.resolveDouble(settings.speedExpr ?: globalMinekart.speedExpr!!) * 0.6 * 2.0
        val topSpeed = if (state.drifting.get()) rideTopSpeed * 0.7 else rideTopSpeed
        val accel = vehicle.runtime.resolveDouble(settings.accelerationExpr ?: globalMinekart.accelerationExpr!!) * 0.3 * 3 * 2.0
        val driftAngle = 20.0f

        //Align the direction of this vector with the vehicles current heading.
        var newVelocity = vehicle.deltaMovement
        newVelocity = newVelocity.yRot(vehicle.yRot.toRadians())

        // rotate the angle of force if drifting
        if (state.drifting.get()) {
            val signedDriftAngle = if(state.clockwiseDrift.get()) driftAngle else driftAngle * -1.0f
            newVelocity = newVelocity.yRot(signedDriftAngle.toRadians())
        }

        //speed up and slow down based on input
        if (driver.zza != 0.0f && state.stamina.get() > 0.0 && !state.inAir.get()) {
            //make sure it can't exceed top speed
            var forwardInput = when {
                driver.zza > 0 && newVelocity.z > topSpeed -> 0.0
                driver.zza < 0 && newVelocity.z <= 0.0 -> 0.0
                else -> driver.zza.sign
            }

            if (!state.drifting.get() && state.boost.get() != 0.0f) {
                forwardInput = 2.0;
            }

            // If drifting we need more acceleration due to the constant turn
            val driftAccelBoost = if (state.drifting.get()) 1.5 else 1.0

            newVelocity = Vec3(
                newVelocity.x,
                newVelocity.y,
                (newVelocity.z + (accel * forwardInput.toDouble() * driftAccelBoost)))
        }

        // Gravity logic
        if (vehicle.onGround()) {
            newVelocity = Vec3(newVelocity.x, 0.0, newVelocity.z)
        } else {
            //TODO: Should we just go back to standard minecraft gravity or do the lerp modifications prevent that?
            //I think minecrafts gravity logic is also too harsh and isn't gamefied enough for mounts maybe? Need
            //to do some testing and get other's opinions
            val gravity = (9.8 / ( 20.0)) * 0.2 * 0.25 * 3.0
            val terminalVel = 2.0

            val fallingForce = gravity -  ( newVelocity.z.sign *gravity *(abs(newVelocity.z) / 2.0))
            newVelocity = Vec3(newVelocity.x, max(newVelocity.y - fallingForce, -terminalVel), newVelocity.z)
        }

        //ground Friction
        var friction = 0.003 * 2.0
        if (state.drifting.get()) {friction *= 0.5}
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
//        val canJump = vehicle.runtime.resolveBoolean(settings.canJump)
//        //Jump the thang!
//        if (driver.jumping && vehicle.onGround() && canJump) {
//            val jumpForce = 0.5
//
//            newVelocity = newVelocity.add(0.0, jumpForce, 0.0)
//
//        }

        // unrotate the angle of force if drifting
        if (state.drifting.get()) {
            val inverseSignedDriftAngle = if(state.clockwiseDrift.get()) driftAngle * -1.0f else driftAngle
            newVelocity = newVelocity.yRot(inverseSignedDriftAngle.toRadians())
        }

        return newVelocity
    }

    override fun angRollVel(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun rotationOnMouseXY(
        settings: MinekartSettings,
        state: MinekartState,
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
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return vehicle.runtime.resolveBoolean(settings.canJump ?: globalMinekart.canJump!!)
    }

    override fun setRideBar(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return 0.0f
    }

    override fun jumpForce(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun gravity(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return 0.0
    }

    override fun rideFovMultiplier(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        var fov = 1.0f
        if(!state.drifting.get() && state.boost.get() != 0.0f) {
            //TODO: is this too abrupt? Possibly caluclate differently?
            fov = 1.2f
        }
        return fov
    }

    override fun useAngVelSmoothing(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun inertia(settings: MinekartSettings, state: MinekartState, vehicle: PokemonEntity): Double {
        return 1.0
    }

    override fun shouldRoll(settings: MinekartSettings, state: MinekartState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun turnOffOnGround(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun dismountOnShift(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotateRiderHead(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity
    ): Boolean {
        return true
    }

    override fun getRideSounds(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity
    ): RideSoundSettingsList {
        return settings.rideSounds
    }

    override fun createDefaultState(settings: MinekartSettings) = MinekartState()

    override fun asMoLangValue(
        settings: MinekartSettings,
        state: MinekartState,
        vehicle: PokemonEntity
    ): ObjectValue<RidingBehaviour<MinekartSettings, MinekartState>> {
        val value = super.asMoLangValue(settings, state, vehicle)
        value.functions.put("drifiting") { DoubleValue(state.drifting.get()) }
        value.functions.put("clockwise_drift") { DoubleValue(state.clockwiseDrift.get()) }
        value.functions.put("boosting") { DoubleValue(state.boost.get()) }
        value.functions.put("in_air") { DoubleValue(state.inAir.get()) }
        return value
    }
}

class MinekartSettings : RidingBehaviourSettings {
    override val key = MinekartBehaviour.KEY
    override val stats = mutableMapOf<RidingStat, IntRange>()

    var infiniteStamina: Expression? = null
        private set
    var canJump: Expression? = null
        private set

    var boostLimit: Expression? = null
        private set

    var speedExpr: Expression? = null
        private set

    // Max accel is a whole 1.0 in 1 second. The conversion in the function below is to convert seconds to ticks
    var accelerationExpr: Expression? = null
        private set

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
        buffer.writeNullableExpression(boostLimit)
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
        boostLimit = buffer.readNullableExpression()
        speedExpr = buffer.readNullableExpression()
        accelerationExpr = buffer.readNullableExpression()
        staminaExpr = buffer.readNullableExpression()
        jumpExpr = buffer.readNullableExpression()
        handlingExpr = buffer.readNullableExpression()
    }
}

class MinekartState : RidingBehaviourState() {
    var currSpeed = ridingState(0.0, Side.BOTH)
    var deltaRotation = ridingState(Vec2.ZERO, Side.BOTH)
    var drifting = ridingState(false, Side.CLIENT)
    var clockwiseDrift = ridingState(false, Side.CLIENT)
    val boost: SidedRidingState<Float> = ridingState(0.0f, Side.CLIENT)
    val turnMomentum: SidedRidingState<Float> = ridingState(0.0f, Side.CLIENT)
    var inAir = ridingState(false, Side.CLIENT)


    override fun reset() {
        super.reset()
        currSpeed.set(0.0, forced = true)
        deltaRotation.set(Vec2.ZERO, forced = true)
        drifting.set(false, forced = true)
        clockwiseDrift.set(false, forced = true)
        boost.set(0.0f, forced = true)
        turnMomentum.set(0.0f, forced = true)
        inAir.set(false, forced = true)
    }

    override fun encode(buffer: FriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeBoolean(drifting.get())
        buffer.writeBoolean(clockwiseDrift.get())
        buffer.writeFloat(boost.get())
        buffer.writeFloat(turnMomentum.get())
        buffer.writeBoolean(inAir.get())
    }

    override fun decode(buffer: FriendlyByteBuf) {
        super.decode(buffer)
        drifting.set(buffer.readBoolean(), forced = true)
        clockwiseDrift.set(buffer.readBoolean(), forced = true)
        boost.set(buffer.readFloat(), forced = true)
        turnMomentum.set(buffer.readFloat(), forced = true)
        inAir.set(buffer.readBoolean(), forced = true)
    }

    override fun toString(): String {
        return "MinekartState(currSpeed=${currSpeed.get()}, deltaRotation=${deltaRotation.get()})"
    }

    override fun copy() = MinekartState().also {
        it.rideVelocity.set(rideVelocity.get(), forced = true)
        it.stamina.set(stamina.get(), forced = true)
        it.currSpeed.set(currSpeed.get(), forced = true)
        it.deltaRotation.set(deltaRotation.get(), forced = true)
        it.drifting.set(drifting.get(), forced = true)
        it.clockwiseDrift.set(clockwiseDrift.get(), forced = true)
        it.boost.set(boost.get(), forced = true)
        it.turnMomentum.set(turnMomentum.get(), forced = true)
        it.inAir.set(inAir.get(), forced = true)
    }

    override fun shouldSync(previous: RidingBehaviourState): Boolean {
        if (previous !is MinekartState) return false
        if (previous.drifting.get() != drifting.get()) return true
        if (previous.clockwiseDrift.get() != clockwiseDrift.get()) return true
        if (previous.boost.get() != boost.get()) return true
        if (previous.turnMomentum.get() != turnMomentum.get()) return true
        if (previous.inAir.get() != inAir.get()) return true
        return super.shouldSync(previous)
    }
}
