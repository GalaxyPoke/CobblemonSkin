/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour.types.air

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonRideSettings
import com.cobblemon.mod.common.OrientationControllable
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourState
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
import com.cobblemon.mod.common.util.resolveDouble
import com.cobblemon.mod.common.util.writeNullableExpression
import com.cobblemon.mod.common.util.writeRidingStats
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes

class HelicopterBehaviour : RidingBehaviour<HelicopterSettings, RidingBehaviourState> {
    companion object {
        val KEY = cobblemonResource("air/helicopter")
        val ROTATION_LIMIT = 30.0f
    }

    override val key = KEY
    val globalHelicopter: HelicopterSettings
        get() = CobblemonRideSettings.helicopter

    override fun getRidingStyle(settings: HelicopterSettings, state: RidingBehaviourState): RidingStyle {
        return RidingStyle.AIR
    }

    val poseProvider = PoseProvider<HelicopterSettings, RidingBehaviourState>(PoseType.HOVER)
        .with(PoseOption(PoseType.FLY) { _, _, entity -> entity.entityData.get(PokemonEntity.MOVING) })

    override fun isActive(settings: HelicopterSettings, state: RidingBehaviourState, vehicle: PokemonEntity): Boolean {
        //If there are only fluid blocks or air block below the ride
        //then activate the controller. If it is in water the ride will
        //dismount accordingly
        return Shapes.create(vehicle.boundingBox).blockPositionsAsListRounded().any {
            if (it.y.toDouble() == (vehicle.position().y)) {
                val blockState = vehicle.level().getBlockState(it.below())
                return@any (blockState.isAir || !blockState.fluidState.isEmpty)
            }
            true

        }
    }

    override fun pose(settings: HelicopterSettings, state: RidingBehaviourState, vehicle: PokemonEntity): PoseType {
        return poseProvider.select(settings, state, vehicle)
    }

    override fun speed(settings: HelicopterSettings, state: RidingBehaviourState, vehicle: PokemonEntity, driver: Player): Float {
        //Increased max speed to exaggerate movement.
        //This likely just needs to be a static number if it really is just
        //the scalar for the velocity vector
        return 2.0f
    }

    override fun rotation(
        settings: HelicopterSettings,
        state: RidingBehaviourState,
        vehicle: PokemonEntity,
        driver: LivingEntity
    ): Vec2 {
        return Vec2(driver.xRot * 0.5f, driver.yRot)
    }

    override fun velocity(
        settings: HelicopterSettings,
        state: RidingBehaviourState,
        vehicle: PokemonEntity,
        driver: Player,
        input: Vec3
    ): Vec3 {
        val runtime = vehicle.runtime

        //If the player is not modulating height then hover
        var yVel = if (driver.jumping) runtime.resolveDouble(settings.verticalVelocity ?: globalHelicopter.verticalVelocity!!)
        else if (driver.isShiftKeyDown) -runtime.resolveDouble(settings.verticalVelocity ?: globalHelicopter.verticalVelocity!!)
        else (0.0)

        yVel *= 0.25
        var xVel = 0.0
        var zVel = 0.0

        val controller = (driver as? OrientationControllable)?.orientationController

        //Horizontal velocity is based on pitch and roll
        if (controller != null) {
            xVel = -1.0 * sin(Math.toRadians(controller.roll.toDouble()))
            zVel = sin(Math.toRadians(controller.pitch.toDouble()))
        }

        return Vec3(xVel, yVel, zVel)
    }

    override fun angRollVel(
        settings: HelicopterSettings,
        state: RidingBehaviourState,
        vehicle: PokemonEntity,
        driver: Player,
        deltaTime: Double
    ): Vec3 {
        val controller = (driver as? OrientationControllable)?.orientationController

        //In degrees per second? It was supposed to be I think but
        //I have messed something up
        val rotationChangeRate = 2.0
        val rotLimit = 30.0

        var pitchForce = driver.zza * rotationChangeRate
        var rollForce = -1.0 * driver.xxa * rotationChangeRate


        //TODO: Fix accumulated movement causing pitch and roll out of set bounds
        if (controller != null) {
            //If the roll or pitch have exceeded the limit then do not modulate that
            //rotation in that direction further

            //modulate the forces so that they trend towards 0 as they approach rotation limit.
            //The current function has it drop off very quickly which may not be ideal.
            //Prev function:
            //      pitchForce = pitchForce * max(rotLim - abs(rollable.pitch.toDouble()), 0.0)

            //The three denotes that the force will be 1/3 what it would have been at
            //the rotation limit
            rollForce *= 2.0.pow(-1.0 * (abs(controller.roll.toDouble()) / ROTATION_LIMIT))

            pitchForce *= 2.0.pow(-1.0 * (abs(controller.pitch.toDouble()) / ROTATION_LIMIT))

            if (controller.roll >= ROTATION_LIMIT && rollForce > 0.0) {
                rollForce = 0.0;
            }
            if (controller.roll <= -ROTATION_LIMIT && rollForce < 0.0) {
                rollForce = 0.0;
            }
            if (controller.pitch >= ROTATION_LIMIT && pitchForce > 0.0) {
                pitchForce = 0.0;
            }
            if (controller.pitch <= -ROTATION_LIMIT && pitchForce < 0.0) {
                pitchForce = 0.0;
            }

            //Ignore x,y,z its angular velocity:
            //yaw, pitch, roll
            return Vec3(0.0, pitchForce.toDouble(), rollForce)
        }

        return Vec3(0.0, 0.0, 0.0)
    }

    override fun rotationOnMouseXY(
        settings: HelicopterSettings,
        state: RidingBehaviourState,
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
        val invertYaw = if (Cobblemon.config.invertYaw) -1 else 1
        //yaw, pitch, roll
        return Vec3(mouseX * invertYaw, 0.0, 0.0)
    }

    override fun canJump(
        settings: HelicopterSettings,
        state: RidingBehaviourState,
        vehicle: PokemonEntity,
        driver: Player
    ): Boolean {
        return false
    }

    override fun setRideBar(
        settings: HelicopterSettings,
        state: RidingBehaviourState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return 0.0f
    }

    override fun jumpForce(
        settings: HelicopterSettings,
        state: RidingBehaviourState,
        vehicle: PokemonEntity,
        driver: Player,
        jumpStrength: Int
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun gravity(
        settings: HelicopterSettings,
        state: RidingBehaviourState,
        vehicle: PokemonEntity,
        regularGravity: Double
    ): Double {
        return 0.0
    }

    override fun rideFovMultiplier(
        settings: HelicopterSettings,
        state: RidingBehaviourState,
        vehicle: PokemonEntity,
        driver: Player
    ): Float {
        return 1.0f
    }

    override fun useAngVelSmoothing(settings: HelicopterSettings, state: RidingBehaviourState, vehicle: PokemonEntity): Boolean {
        return true
    }

    override fun inertia(settings: HelicopterSettings, state: RidingBehaviourState, vehicle: PokemonEntity): Double {
        return 0.1
    }

    override fun shouldRoll(settings: HelicopterSettings, state: RidingBehaviourState, vehicle: PokemonEntity): Boolean {
        return true
    }

    override fun turnOffOnGround(settings: HelicopterSettings, state: RidingBehaviourState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun dismountOnShift(settings: HelicopterSettings, state: RidingBehaviourState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(
        settings: HelicopterSettings,
        state: RidingBehaviourState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun shouldRotateRiderHead(
        settings: HelicopterSettings,
        state: RidingBehaviourState,
        vehicle: PokemonEntity
    ): Boolean {
        return false
    }

    override fun getRideSounds(settings: HelicopterSettings, state: RidingBehaviourState, vehicle: PokemonEntity): RideSoundSettingsList {
        return settings.rideSounds
    }

    override fun createDefaultState(settings: HelicopterSettings) = RidingBehaviourState()
}


class HelicopterSettings : RidingBehaviourSettings {
    override val key = HelicopterBehaviour.KEY
    override val stats = mutableMapOf<RidingStat, IntRange>()

    var gravity: Expression? = null
        private set

    var horizontalAcceleration: Expression? = null
        private set

    var verticalVelocity: Expression? = null
        private set

    var speed: Expression? = null
        private set

    var rideSounds: RideSoundSettingsList = RideSoundSettingsList()

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeRidingStats(stats)
        rideSounds.encode(buffer)
        buffer.writeNullableExpression(gravity)
        buffer.writeNullableExpression(horizontalAcceleration)
        buffer.writeNullableExpression(verticalVelocity)
        buffer.writeNullableExpression(speed)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        stats.putAll(buffer.readRidingStats())
        rideSounds = RideSoundSettingsList.decode(buffer)
        gravity = buffer.readNullableExpression()
        horizontalAcceleration = buffer.readNullableExpression()
        verticalVelocity = buffer.readNullableExpression()
        speed = buffer.readNullableExpression()
    }
}
