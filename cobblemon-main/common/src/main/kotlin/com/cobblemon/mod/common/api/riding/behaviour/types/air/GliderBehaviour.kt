/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour.types.air

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.CobblemonRideSettings
import com.cobblemon.mod.common.OrientationControllable
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourState
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviour
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.riding.posing.PoseOption
import com.cobblemon.mod.common.api.riding.posing.PoseProvider
import com.cobblemon.mod.common.api.riding.sound.RideSoundSettingsList
import com.cobblemon.mod.common.api.riding.stats.RidingStat
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.util.SmoothDouble
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

class GliderBehaviour : RidingBehaviour<GliderSettings, RidingBehaviourState> {
    companion object {
        val KEY = cobblemonResource("air/glider")
    }

    override val key = KEY

    val globalGlider: GliderSettings
        get() = CobblemonRideSettings.glider

    override fun getRidingStyle(settings: GliderSettings, state: RidingBehaviourState): RidingStyle {
        return RidingStyle.AIR
    }

    val poseProvider = PoseProvider<GliderSettings, RidingBehaviourState>(PoseType.HOVER)
            .with(PoseOption(PoseType.FLY) { _, _, entity -> entity.entityData.get(PokemonEntity.MOVING) })

    override fun isActive(settings: GliderSettings, state: RidingBehaviourState, vehicle: PokemonEntity): Boolean {
        return true
    }

    override fun pose(settings: GliderSettings, state: RidingBehaviourState, vehicle: PokemonEntity): PoseType {
        return poseProvider.select(settings, state, vehicle)
    }

    override fun speed(settings: GliderSettings, state: RidingBehaviourState, vehicle: PokemonEntity, driver: Player): Float {
        return vehicle.runtime.resolveFloat(settings.speed ?: globalGlider.speed!!)
    }

    override fun rotation(
            settings: GliderSettings,
            state: RidingBehaviourState,
            vehicle: PokemonEntity,
            driver: LivingEntity
    ): Vec2 {
        return Vec2(driver.xRot, driver.yRot)
    }

    override fun velocity(
            settings: GliderSettings,
            state: RidingBehaviourState,
            vehicle: PokemonEntity,
            driver: Player,
            input: Vec3
    ): Vec3 {
        val xVector = if (vehicle.runtime.resolveBoolean(settings.canStrafe ?: globalGlider.canStrafe!!)) driver.xxa.toDouble() else 0.0
        val yVector = -vehicle.runtime.resolveDouble(settings.glideSpeed ?: globalGlider.glideSpeed!!)
        val zVector = driver.zza.toDouble()
        return Vec3(xVector, yVector, zVector)
    }

    override fun angRollVel(
            settings: GliderSettings,
            state: RidingBehaviourState,
            vehicle: PokemonEntity,
            driver: Player,
            deltaTime: Double
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun rotationOnMouseXY(
            settings: GliderSettings,
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

        //Might need to add the smoothing here for default.
        return Vec3(0.0, mouseY, mouseX)
    }

    override fun canJump(settings: GliderSettings, state: RidingBehaviourState, vehicle: PokemonEntity, driver: Player): Boolean {
        return false
    }

    override fun setRideBar(
            settings: GliderSettings,
            state: RidingBehaviourState,
            vehicle: PokemonEntity,
            driver: Player
    ): Float {
        return 0.0f
    }

    override fun jumpForce(
            settings: GliderSettings,
            state: RidingBehaviourState,
            vehicle: PokemonEntity,
            driver: Player,
            jumpStrength: Int
    ): Vec3 {
        return Vec3.ZERO
    }

    override fun gravity(
            settings: GliderSettings,
            state: RidingBehaviourState,
            vehicle: PokemonEntity,
            regularGravity: Double
    ): Double {
        return 0.0
    }

    override fun rideFovMultiplier(
            settings: GliderSettings,
            state: RidingBehaviourState,
            vehicle: PokemonEntity,
            driver: Player
    ): Float {
        return 1.0f
    }

    override fun useAngVelSmoothing(settings: GliderSettings, state: RidingBehaviourState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun inertia(settings: GliderSettings, state: RidingBehaviourState, vehicle: PokemonEntity): Double {
        return 0.5
    }

    override fun shouldRoll(settings: GliderSettings, state: RidingBehaviourState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun turnOffOnGround(settings: GliderSettings, state: RidingBehaviourState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun dismountOnShift(settings: GliderSettings, state: RidingBehaviourState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun shouldRotatePokemonHead(settings: GliderSettings, state: RidingBehaviourState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun shouldRotateRiderHead(settings: GliderSettings, state: RidingBehaviourState, vehicle: PokemonEntity): Boolean {
        return false
    }

    override fun getRideSounds(settings: GliderSettings, state: RidingBehaviourState, vehicle: PokemonEntity): RideSoundSettingsList {
        return settings.rideSounds
    }

    override fun createDefaultState(settings: GliderSettings) = RidingBehaviourState()
}

class GliderSettings : RidingBehaviourSettings {
    override val key = GliderBehaviour.KEY
    override val stats = mutableMapOf<RidingStat, IntRange>()

    var glideSpeed: Expression? = null
        private set

    var speed: Expression? = null
        private set

    var canStrafe: Expression? = null
        private set

    var rideSounds: RideSoundSettingsList = RideSoundSettingsList()

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeRidingStats(stats)
        rideSounds.encode(buffer)
        buffer.writeNullableExpression(glideSpeed)
        buffer.writeNullableExpression(speed)
        buffer.writeNullableExpression(canStrafe)
    }

    override fun decode(buffer: RegistryFriendlyByteBuf) {
        stats.putAll(buffer.readRidingStats())
        rideSounds = RideSoundSettingsList.decode(buffer)
        glideSpeed = buffer.readNullableExpression()
        speed = buffer.readNullableExpression()
        canStrafe = buffer.readNullableExpression()
    }
}
