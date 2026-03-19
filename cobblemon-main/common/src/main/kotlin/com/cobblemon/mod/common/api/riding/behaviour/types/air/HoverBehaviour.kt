/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour.types.air

import com.bedrockk.molang.Expression
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
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.joml.Matrix3f
import kotlin.math.*

class HoverBehaviour : RidingBehaviour<HoverSettings, HoverState> {
    companion object {
        val KEY = cobblemonResource("air/hover")
    }

    override val key = KEY
    val globalHover: HoverSettings
        get() = CobblemonRideSettings.hover

    override fun getRidingStyle(settings: HoverSettings, state: HoverState): RidingStyle {
        return RidingStyle.AIR
    }

    val poseProvider = PoseProvider<HoverSettings, HoverState>(PoseType.STAND)
        .with(PoseOption(PoseType.WALK) { _, state, _ ->
            return@PoseOption abs(state.rideVelocity.get().length()) > 0.01
        })

    override fun isActive(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Boolean {
        // Hover mounts should be able to go just about anywhere but underwater
        return !vehicle.isUnderWater
    }

    override fun pose(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): PoseType {
        return this.poseProvider.select(settings, state, vehicle)
    }

    override fun speed(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return state.speed.get().toFloat()
    }

    override fun tick(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ) {
        if(vehicle.level().isClientSide) {
            tickStamina(settings, state, vehicle)
            checkTooHigh(settings, state, vehicle)
        }
    }

    fun checkTooHigh(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
    ) {
        val heightLimit = vehicle.runtime.resolveDouble(settings.jumpExpr ?: globalHover.jumpExpr!!)
        val pos = vehicle.position()
        val level = Minecraft.getInstance().player?.level() ?: return
        val hit = level.clip(
            ClipContext(
                pos,
                pos.subtract(0.0, heightLimit, 0.0),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                vehicle
            )
        )

        state.tooHigh.set(hit.type == HitResult.Type.MISS)
    }

    fun tickStamina(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ) {
        if (vehicle.runtime.resolveBoolean(settings.infiniteStamina ?: globalHover.infiniteStamina!!)) {
            return
        }
        val stam = state.stamina.get()
        val stamDrainRate = (1.0f / vehicle.runtime.resolveDouble(settings.staminaExpr ?: globalHover.staminaExpr!!)).toFloat() / 20.0f
        val stamReplenishRate = (1.0f / vehicle.runtime.resolveDouble(settings.stamReplenishTimeSeconds ?: globalHover.stamReplenishTimeSeconds!!) ).toFloat() / 20.0f // 2 seconds to replenish a full bar of stamina
        val newStam = if (state.tooHigh.get()) max(0.0f, stam - stamDrainRate) else min(1.0f,stam + stamReplenishRate)

        state.stamina.set(newStam)
    }

    override fun updatePassengerRotation(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ) {
        return
    }

    override fun clampPassengerRotation(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ) {
        return
    }


    override fun rotation(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {
        val turnAmount =  calcRotAmount(settings, state, vehicle, driver)
        state.rideVelocity.set( state.rideVelocity.get().yRot(turnAmount.toRadians()))
        return Vec2(driver.xRot, vehicle.yRot + turnAmount )
    }

    /*
   *  Calculates the rotation amount given the difference between
   *  the current player y rot and entity y rot. This gives the
   *  affect of influencing a rides rotation through the mouse
   *  without it being instant.
   */
    fun calcRotAmount(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Float {
        val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr ?: globalHover.speedExpr!!) / 20.0
        val handling = vehicle.runtime.resolveDouble(settings.handlingExpr ?: globalHover.handlingExpr!!) / 20.0
        val maxYawDiff = 90.0f

        // Normalize the current rotation diff
        val rotDiff = Mth.wrapDegrees(driver.yRot - vehicle.yRot).coerceIn(-maxYawDiff,maxYawDiff)
        val rotDiffInfl = sqrt(abs(rotDiff / maxYawDiff)) * rotDiff.sign

        var turnRate = (handling.toFloat()) * rotDiffInfl
        turnRate -= (turnRate * 0.5f) * (RidingBehaviour.scaleToRange(vehicle.deltaMovement.length(), 0.0, topSpeed).coerceIn(0.0, 1.0)).pow(2).toFloat()

        // Ensure you only ever rotate as much difference as there is between the angles.
        val turnSpeed = turnRate
        val rotAmount = turnSpeed.coerceIn(-abs(rotDiff), abs(rotDiff))

        return rotAmount
    }

    override fun velocity(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        val retVel = calculateRideSpaceVel(settings, state, vehicle, driver)
        state.rideVelocity.set(retVel)
        return retVel
    }

    private fun calculateRideSpaceVel(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player
    ): Vec3 {
        // Convert these to their tick values
        val topSpeed = vehicle.runtime.resolveDouble(settings.speedExpr ?: globalHover.speedExpr!!) / 20.0
        val accel = topSpeed / (vehicle.runtime.resolveDouble(settings.accelerationExpr ?: globalHover.accelerationExpr!!) * 20.0)

        var newVelocity = state.rideVelocity.get()
        val unrotateVel = vehicle.deltaMovement.yRot(vehicle.yRot)

        val vehicleFrontVec = Vec3(0.0, 0.0, 1.0)
        val faceVel = Vec3(vehicle.deltaMovement.x, 0.0, vehicle.deltaMovement.z)

        val d = vehicle.deltaMovement.horizontalDistance()
        val v2 = Vec3(faceVel.dot(Vec3(1.0, 0.0, 0.0)) * d, newVelocity.y, faceVel.dot(Vec3(0.0, 0.0, 1.0)) * d)

        // Check for collisions and if found, update the speed to reflect it
        if (vehicle.horizontalCollision) {
            val smackingIntoWallSpeed = max( vehicle.deltaMovement.horizontalDistance(), 0.2)
            newVelocity = Vec3(newVelocity.x, 0.0, newVelocity.z).normalize().scale(smackingIntoWallSpeed).add(0.0, newVelocity.y, 0.0)
        }


        if (vehicle.verticalCollision) {
            newVelocity = Vec3(newVelocity.x, 0.0, newVelocity.z)
        }

        // align the velocity vector to be in local vehicle space
//        val yawAligned = Matrix3f().rotateY(vehicle.yRot.toRadians())
//        newVelocity = (newVelocity.toVector3f().mul(yawAligned)).toVec3d()

        // Vertical movement based on driver input.
        val vertInput = when {
            state.stamina.get() == 0.0f -> -1.0
            driver.jumping -> 1.0
            driver.isShiftKeyDown -> -1.0
            else -> 0.0
        }

        // Normalize and scale to accel so that diagonal movement
        // doesn't speed up the ride more than input in one direction
        var inputVel = Vec3(
            driver.xxa.sign.toDouble(),
            vertInput,
            driver.zza.sign.toDouble()
        ).normalize()

        // Air Friction/Resistance
        // When the dot between friction and input is -1 we want to apply none of it.
        // In short we want to avoid applying friction that opposes our input force.
        // otherwise we would not be able to reach our top speed when acceleration is
        // low.
        val frictionConst = 0.02
        var frictionForce = newVelocity.scale(-1.0).scale(frictionConst)
        val frictionApplied = if(inputVel.lengthSqr() != 0.0) 1 - (0.5*(1 + -1.0 * (inputVel.normalize().dot(frictionForce.normalize()))))
            else 1.0
        frictionForce = frictionForce.scale((frictionApplied))
        newVelocity = newVelocity.add(frictionForce)

        // Ensure that if you're not applying force where the ride is facing then you apply a portion of debuff
        val strafeDebuff = 0.5
        val vehicleForwardVec = Vec3(0.0, 0.0, 1.0)
        val playerForwardVec = inputVel //.yRot(vehicle.yRot)
        val inputStrength = accel - (accel * strafeDebuff*(1 - vehicleForwardVec.dot(playerForwardVec).coerceIn(0.0, 1.0)))
        inputVel = inputVel.scale(inputStrength)

        newVelocity = newVelocity.add(inputVel)

        // Set speed to new magnitude to avoid the horrible things minecraft friction
        // does to our vectors :(
        state.speed.set(min(topSpeed, newVelocity.length()))

        // Check to see if this new velocity will exceed top speed and if it will then cap it
        newVelocity = if (newVelocity.length() > topSpeed) newVelocity.normalize().scale(topSpeed) else newVelocity

        return newVelocity
    }

    override fun angRollVel(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun rotationOnMouseXY(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player,
        mouseY: Double,
        mouseX: Double,
        mouseYSmoother: SmoothDouble,
        mouseXSmoother: SmoothDouble,
        sensitivity: Double,
        deltaTime: Double
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun canJump(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return false
    }

    override fun setRideBar(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        //Retrieve stamina from state and use it to set the "stamina bar"
        return (state.stamina.get() / 1.0f)
    }

    override fun jumpForce(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun gravity(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return 0.0
    }

    override fun rideFovMultiplier(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return if (state.isBoosting.get()) 1.1f else 1.0f
    }

    override fun useAngVelSmoothing(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun inertia(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Double {
        return 1.0
    }

    override fun shouldRoll(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun turnOffOnGround(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun dismountOnShift(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotateRiderHead(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): Boolean {
        return true
    }

    override fun getRideSounds(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): RideSoundSettingsList {
        return settings.rideSounds
    }

    override fun createDefaultState(settings: HoverSettings) = HoverState()

    override fun asMoLangValue(
        settings: HoverSettings,
        state: HoverState,
        vehicle: PokemonEntity
    ): ObjectValue<RidingBehaviour<HoverSettings, HoverState>> {
        val value = super.asMoLangValue(settings, state, vehicle)
        value.functions.put("boosting") { DoubleValue(state.isBoosting.get()) }
        value.functions.put("boost_ticks") { DoubleValue(state.boostTicks.get()) }
        value.functions.put("too_high") { DoubleValue(state.tooHigh.get()) }
        return value
    }
}

class HoverSettings : RidingBehaviourSettings {
    override val key = HoverBehaviour.KEY
    override val stats = mutableMapOf<RidingStat, IntRange>()

    var infiniteStamina: Expression? = null
        private set

    var canJump: Expression? = null
        private set

    var stamReplenishTimeSeconds: Expression? = null
        private set

    // Speed in block per second
    var speedExpr: Expression? = null
        private set

    // Acceleration in number of seconds to top speed
    var accelerationExpr: Expression? = null
        private set

    // Amount of seconds between boosts
    var staminaExpr: Expression? = null
        private set

    // Blocks before stamina drain
    var jumpExpr: Expression? = null
        private set

    // Turn rate in degrees per second when stationary
    var handlingExpr: Expression? = null
        private set

    var rideSounds: RideSoundSettingsList = RideSoundSettingsList()

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeRidingStats(stats)
        rideSounds.encode(buffer)
        buffer.writeNullableExpression(infiniteStamina)
        buffer.writeNullableExpression(canJump)
        buffer.writeNullableExpression(stamReplenishTimeSeconds)
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
        stamReplenishTimeSeconds = buffer.readNullableExpression()
        speedExpr = buffer.readNullableExpression()
        accelerationExpr = buffer.readNullableExpression()
        staminaExpr = buffer.readNullableExpression()
        jumpExpr = buffer.readNullableExpression()
        handlingExpr = buffer.readNullableExpression()
    }

}

class HoverState : RidingBehaviourState() {
    var speed = ridingState(0.0, Side.CLIENT)
    var isBoosting = ridingState(false, Side.BOTH)
    var boostVec = ridingState(Vec3.ZERO, Side.CLIENT)
    var boostTicks = ridingState(0, Side.CLIENT)
    var tooHigh = ridingState(false, Side.CLIENT)

    override fun encode(buffer: FriendlyByteBuf) {
        super.encode(buffer)
        buffer.writeDouble(speed.get())
        buffer.writeBoolean(isBoosting.get())
        buffer.writeBoolean(tooHigh.get())
    }

    override fun decode(buffer: FriendlyByteBuf) {
        super.decode(buffer)
        speed.set(buffer.readDouble(), forced = true)
        isBoosting.set(buffer.readBoolean(), forced = true)
        tooHigh.set(buffer.readBoolean(), forced = true)
    }

    override fun reset() {
        super.reset()
        speed.set(0.0, forced = true)
        isBoosting.set(false, forced = true)
        boostVec.set(Vec3.ZERO, forced = true)
        boostTicks.set(0, forced = true)
        tooHigh.set(false, forced = true)
    }

    override fun copy() = HoverState().also {
        it.rideVelocity.set(this.rideVelocity.get(), forced = true)
        it.stamina.set(this.stamina.get(), forced = true)
        it.isBoosting.set(this.isBoosting.get(), forced = true)
        it.boostVec.set(this.boostVec.get(), forced = true)
        it.boostTicks.set(this.boostTicks.get(), forced = true)
        it.tooHigh.set(this.tooHigh.get(), forced = true)
    }

    override fun shouldSync(previous: RidingBehaviourState): Boolean {
        if (previous !is HoverState) return false
        if (previous.isBoosting.get() != isBoosting.get()) return true
        return super.shouldSync(previous)
    }
}
