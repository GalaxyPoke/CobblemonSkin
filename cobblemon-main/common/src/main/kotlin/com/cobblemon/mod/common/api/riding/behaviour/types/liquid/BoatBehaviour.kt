/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour.types.liquid

import com.bedrockk.molang.Expression
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
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.util.Mth
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes

class BoatBehaviour : RidingBehaviour<BoatSettings, BoatState> {
    companion object {
        val KEY = cobblemonResource("liquid/boat")
    }

    override val key = KEY
    val globalBoat: BoatSettings
        get() = CobblemonRideSettings.boat

    override fun getRidingStyle(settings: BoatSettings, state: BoatState): RidingStyle {
        return RidingStyle.LIQUID
    }

    val poseProvider = PoseProvider<BoatSettings, BoatState>(PoseType.STAND)
        .with(PoseOption(PoseType.WALK) { _, state, _ ->
            abs(state.rideVelocity.get().horizontalDistance()) > 0.2
        })

    override fun isActive(settings: BoatSettings, state: BoatState, vehicle: PokemonEntity): Boolean {
        if (state.jumpBuffer.get() != -1) {
            return true
        }
        return Shapes.create(vehicle.boundingBox).blockPositionsAsListRounded().any {
            if (vehicle.isInWater || vehicle.isUnderWater) {
                return@any true
            }
            val blockState = vehicle.level().getBlockState(it)
            return@any !blockState.fluidState.isEmpty
        }
    }

    override fun tick(
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ) {
        if (state.jumpBuffer.get() > 5) {
            if ((vehicle.isInWater || vehicle.isUnderWater || vehicle.onGround())) {
                state.jumpBuffer.set(-1)
            }
        }
        else if (state.jumpBuffer.get() > -1) {
            state.jumpBuffer.set(state.jumpBuffer.get() + 1)
        }

        // Only start sprinting on vehicle if the state has changed and the pokemon has atleast 1/3 stamina
        if (driver.isSprinting && !state.isVehicleSprinting.get()) {
            state.isVehicleSprinting.set(state.stamina.get() > 0.33f)
        }
        else if (!driver.isSprinting) {
            state.isVehicleSprinting.set(false)
        }

        if (!state.isVehicleSprinting.get()) {
            if (state.staminaBuffer.get() >= 20) {
                val staminaIncrease = (1.0f / vehicle.runtime.resolveDouble(settings.stamReplenishTimeSeconds ?: globalBoat.stamReplenishTimeSeconds!!) ).toFloat() / 20.0f
                state.stamina.set(min(1.0f, state.stamina.get() + staminaIncrease))
            }
        }
        else if (abs(state.rideVelocity.get().z) > 0.1 && state.isVehicleSprinting.get()) {
            consumeStamina(vehicle, driver, settings, state)
        }
        state.staminaBuffer.set(state.staminaBuffer.get() + 1)
        super.tick(settings, state, vehicle, driver, input)
    }

    override fun pose(settings: BoatSettings, state: BoatState, vehicle: PokemonEntity): PoseType {
        return poseProvider.select(settings, state, vehicle)
    }

    override fun speed(settings: BoatSettings, state: BoatState, vehicle: PokemonEntity, driver: Player): Float {
        return state.rideVelocity.get().length().toFloat()
    }

    override fun rotation(
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {
        state.deltaRotation.set(state.deltaRotation.get() * 0.8)
        return Vec2(vehicle.xRot, vehicle.yRot + state.deltaRotation.get().toFloat())
    }

    override fun velocity(
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        applyStrafeRotation(vehicle, driver, settings, state)

        var velocity = state.rideVelocity.get()
        velocity = applyVelocityFromInput(velocity, vehicle, driver, settings, state)
        velocity = applyGravity(velocity, vehicle, settings, state)
        velocity = applyJump(velocity, vehicle, driver, settings, state)
        state.rideVelocity.set(velocity)
        return state.rideVelocity.get()
    }

    private fun applyVelocityFromInput(velocity: Vec3, vehicle: PokemonEntity, driver: Player, settings: BoatSettings, state: BoatState): Vec3 {
        val speed = vehicle.runtime.resolveDouble(settings.speedExpr ?: globalBoat.speedExpr!!)
        val acceleration = speed / (vehicle.runtime.resolveDouble(settings.accelerationExpr ?: globalBoat.accelerationExpr!!) * 20)

        if (state.jumpBuffer.get() != -1 || !(vehicle.isInWater || vehicle.isUnderWater)) {
            return velocity
        }

        val sprintModifier = vehicle.runtime.resolveDouble(settings.sprintSpeedModifier ?: globalBoat.sprintSpeedModifier!!)
        val activeSprintModifier = if (state.isVehicleSprinting.get() && hasStamina(state)) sprintModifier else 1.0
        val forwardInput = driver.zza.toDouble()
        val delta = when {
            forwardInput == 0.0 -> -velocity.z * 0.04
            forwardInput < 0.0 -> when {
                velocity.z < 0.0 -> forwardInput * acceleration * 0.5
                else -> min(forwardInput * acceleration * 0.5, -velocity.z * 0.04) // We never want it to be slower to reverse than no input
            }
            else -> forwardInput * acceleration * activeSprintModifier
        }

        return Vec3(
            velocity.x,
            velocity.y,
            Mth.clamp(velocity.z + delta, -speed * 0.25, speed * activeSprintModifier)
        )
    }

    private fun applyJump(velocity: Vec3, vehicle: PokemonEntity, driver: Player, settings: BoatSettings, state: BoatState): Vec3 {
        if (!driver.jumping) return velocity // Not jumping
        if (state.jumpBuffer.get() != -1) return velocity // Already jumped very recently

        val jumpStrength = vehicle.runtime.resolveDouble(settings.jumpStrengthExpr ?: globalBoat.jumpStrengthExpr!!)
        state.jumpBuffer.set(0)
        return Vec3(velocity.x, velocity.y + jumpStrength, velocity.z)
    }

    private fun applyGravity(velocity: Vec3, vehicle: PokemonEntity, settings: BoatSettings, state: BoatState): Vec3 {
        if (state.jumpBuffer.get() == -1 && (vehicle.isInWater || vehicle.isUnderWater)) {
            if (shouldFloatHigher(vehicle, settings)) {
                return Vec3(velocity.x, 0.5, velocity.z)
            }
            else {
                return Vec3(velocity.x, 0.0, velocity.z)
            }
        }
        val terminalVelocity = vehicle.runtime.resolveDouble(settings.terminalVelocity ?: globalBoat.terminalVelocity!!)
        val gravity = (9.8 / ( 20.0)) * 0.2
        return Vec3(velocity.x, max(velocity.y - gravity, terminalVelocity), velocity.z)
    }

    private fun shouldFloatHigher(vehicle: PokemonEntity, settings: BoatSettings): Boolean {
        val surfaceOffset = vehicle.runtime.resolveFloat(settings.surfaceLevelOffset ?: globalBoat.surfaceLevelOffset!!)
        val blockPos = BlockPos.containing(vehicle.x, vehicle.eyeY + surfaceOffset, vehicle.z)
        val fluidState = vehicle.level().getFluidState(blockPos)
        return !fluidState.isEmpty
    }

    private fun applyStrafeRotation(vehicle: PokemonEntity, driver: Player, settings: BoatSettings, state: BoatState) {
        val skillModifier = vehicle.runtime.resolveDouble(settings.rotationSpeedModifierExpr ?: globalBoat.rotationSpeedModifierExpr!!)
        val strafe = driver.xxa
        if (abs(strafe) > 0) {
            val increase = if (strafe > 0) -1 else 1
            state.deltaRotation.set(state.deltaRotation.get() + increase * skillModifier)
        }
    }

    private fun hasStamina(state: BoatState): Boolean {
        return state.stamina.get() != 0f
    }

    private fun consumeStamina(vehicle: PokemonEntity, driver: Player, settings: BoatSettings, state: BoatState) {
        if (vehicle.runtime.resolveBoolean(settings.infiniteStamina ?: globalBoat.infiniteStamina!!)) {
            return
        }
        val stamDrainRate = (1.0f / vehicle.runtime.resolveDouble(settings.staminaExpr ?: globalBoat.staminaExpr!!)).toFloat() / 20.0f
        state.stamina.set(max(0f, state.stamina.get() - stamDrainRate))
        if (state.stamina.get() == 0f) {
            state.isVehicleSprinting.set(false)
            driver.isSprinting = false
        }
        state.staminaBuffer.set(0)
    }

    override fun updatePassengerRotation(
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ) {
        driver.yRot += state.deltaRotation.get().toFloat() * 0.887f
        driver.yHeadRot += state.deltaRotation.get().toFloat() * 0.887f
        clampPassengerRotation(settings, state, vehicle, driver)
    }

    override fun clampPassengerRotation(
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ) {
        driver.setYBodyRot(vehicle.yRot)
        val f: Float = Mth.wrapDegrees(driver.yRot - vehicle.yRot)
        val g = Mth.clamp(f, -105.0f, 105.0f)
        driver.yRotO += g - f
        driver.yRot = driver.yRot + g - f
        driver.setYHeadRot(driver.yRot)
        vehicle.setYHeadRot(driver.yRot)
    }

    override fun angRollVel(
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun rotationOnMouseXY(
        settings: BoatSettings,
        state: BoatState,
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
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return false
    }

    override fun setRideBar(
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return (state.stamina.get() / 1.0f)
    }

    override fun jumpForce(
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun gravity(
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return 0.0
    }

    override fun rideFovMultiplier(
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        val sprintFov = vehicle.runtime.resolveFloat(settings.sprintFovModifier ?: globalBoat.sprintFovModifier!!)
        return if (state.isVehicleSprinting.get()) sprintFov else 1.0f
    }

    override fun useAngVelSmoothing(settings: BoatSettings, state: BoatState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun inertia(settings: BoatSettings, state: BoatState, vehicle: PokemonEntity): Double {
        return 0.5
    }

    override fun shouldRoll(settings: BoatSettings, state: BoatState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun turnOffOnGround(settings: BoatSettings, state: BoatState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun dismountOnShift(settings: BoatSettings, state: BoatState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity
    ): Boolean {
        return vehicle.runtime.resolveBoolean(settings.rotatePokemonHead ?: globalBoat.rotatePokemonHead!!)
    }

    override fun shouldRotateRiderHead(
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity
    ): Boolean {
        return true
    }

    override fun getRideSounds(
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity
    ): RideSoundSettingsList {
        return settings.rideSounds
    }

    override fun maxUpStep(settings: BoatSettings, state: BoatState, vehicle: PokemonEntity) = 0f

    override fun createDefaultState(settings: BoatSettings) = BoatState()

    override fun asMoLangValue(
        settings: BoatSettings,
        state: BoatState,
        vehicle: PokemonEntity
    ): ObjectValue<RidingBehaviour<BoatSettings, BoatState>> {
        val value = super.asMoLangValue(settings, state, vehicle)
        value.functions.put("sprinting") { DoubleValue(state.isVehicleSprinting.get()) }
        value.functions.put("in_air") {
            val isInWater = Shapes.create(vehicle.boundingBox).blockPositionsAsListRounded().any {
                       if (vehicle.isInWater || vehicle.isUnderWater) {
                           return@any true
                       }
                       val blockState = vehicle.level().getBlockState(it)
                       return@any !blockState.fluidState.isEmpty
                   }
            DoubleValue(!isInWater)
        }
        return value
    }
}

class BoatSettings : RidingBehaviourSettings {
    override val key = BoatBehaviour.KEY
    override val stats = mutableMapOf<RidingStat, IntRange>()
    var rideSounds: RideSoundSettingsList = RideSoundSettingsList()

    var infiniteStamina: Expression? = null
        private set

    var terminalVelocity: Expression? = null
        private set

    var rotatePokemonHead: Expression? = null
        private set

    var stamReplenishTimeSeconds: Expression? = null
        private set

    var staminaExpr: Expression? = null
        private set

    var rotationSpeedModifierExpr: Expression? = null
        private set

    var jumpStrengthExpr: Expression? = null
        private set

    var speedExpr: Expression? = null
        private set

    var accelerationExpr: Expression? = null
        private set

    var sprintSpeedModifier: Expression? = null
        private set

    var sprintFovModifier: Expression? = null

    var surfaceLevelOffset: Expression? = null

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeRidingStats(stats)
        rideSounds.encode(buffer)
        buffer.writeNullableExpression(infiniteStamina)
        buffer.writeNullableExpression(terminalVelocity)
        buffer.writeNullableExpression(rotatePokemonHead)
        buffer.writeNullableExpression(stamReplenishTimeSeconds)
        buffer.writeNullableExpression(staminaExpr)
        buffer.writeNullableExpression(rotationSpeedModifierExpr)
        buffer.writeNullableExpression(jumpStrengthExpr)
        buffer.writeNullableExpression(speedExpr)
        buffer.writeNullableExpression(accelerationExpr)
        buffer.writeNullableExpression(sprintSpeedModifier)
        buffer.writeNullableExpression(sprintFovModifier)
        buffer.writeNullableExpression(surfaceLevelOffset)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        stats.putAll(buffer.readRidingStats())
        rideSounds = RideSoundSettingsList.decode(buffer)
        infiniteStamina = buffer.readNullableExpression()
        terminalVelocity = buffer.readNullableExpression()
        rotatePokemonHead = buffer.readNullableExpression()
        stamReplenishTimeSeconds = buffer.readNullableExpression()
        staminaExpr = buffer.readNullableExpression()
        rotationSpeedModifierExpr = buffer.readNullableExpression()
        jumpStrengthExpr = buffer.readNullableExpression()
        speedExpr = buffer.readNullableExpression()
        accelerationExpr = buffer.readNullableExpression()
        sprintSpeedModifier = buffer.readNullableExpression()
        sprintFovModifier = buffer.readNullableExpression()
        surfaceLevelOffset = buffer.readNullableExpression()
    }
}

class BoatState : RidingBehaviourState() {
    val deltaRotation = ridingState(0.0, Side.CLIENT)
    val jumpBuffer = ridingState(-1, Side.CLIENT)
    val staminaBuffer = ridingState(0, Side.CLIENT)
    val isVehicleSprinting = ridingState(false, Side.CLIENT)

    override fun copy() = BoatState().also {
        it.deltaRotation.set(deltaRotation.get(), true)
        it.rideVelocity.set(rideVelocity.get(), true)
        it.stamina.set(stamina.get(), true)
        it.jumpBuffer.set(jumpBuffer.get(), true)
        it.staminaBuffer.set(staminaBuffer.get(), true)
        it.isVehicleSprinting.set(isVehicleSprinting.get(), true)
    }

    override fun shouldSync(previous: RidingBehaviourState): Boolean {
        if (previous !is BoatState) return false
        return super.shouldSync(previous)
    }
}
