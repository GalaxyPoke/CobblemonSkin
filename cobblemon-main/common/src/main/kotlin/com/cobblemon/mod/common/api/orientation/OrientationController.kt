/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.orientation

import com.cobblemon.mod.common.util.math.geometry.toDegrees
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import org.joml.Matrix3f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.sign

open class OrientationController(val entity: LivingEntity) {

    companion object {
        val FORWARDS: Vector3f = Vector3f(0.0f, 0.0f, -1.0f)
        val UP: Vector3f = Vector3f(0.0f, 1.0f, 0.0f)
        val LEFT: Vector3f = Vector3f(-1.0f, 0.0f, 0.0f)
    }

    open var active: Boolean = false

    var orientation: Matrix3f? = null
        private set

    private var renderOrientationO: Matrix3f? = null
    private var renderOrientation: Matrix3f? = null

    /** Adding this simply because it irritates me seeing `getActive` when most of this is Java. */
    fun isActive() = active

    fun updateOrientation(fn: (Matrix3f) -> Matrix3f?) {
        if (!active) return
        if (orientation == null) {
            orientation = Matrix3f()
            rotate(entity.yRot - 180, entity.xRot, 0f)
        }
        orientation = fn(orientation!!)
        entity.yRot = yaw
        entity.xRot = pitch
    }

    fun reset() {
        orientation = null
        renderOrientationO = null
        renderOrientation = null
        active = false
    }

    fun rotate(yaw: Float, pitch: Float, roll: Float) {
        rotateYaw(yaw)
        rotatePitch(pitch)
        rotateRoll(roll)
    }

    fun getRenderOrientation(delta: Float): Quaternionf {
        // Return direct orientation if this is the player that drives this entities orientation.
        //TODO: Why does this break the render orientation.
//        if(entity.level().isClientSide && entity.controllingPassenger == Minecraft.getInstance().player) {
//            return Quaternionf().setFromUnnormalized(orientation)
//        }
        val old = renderOrientationO ?: renderOrientation ?: orientation ?: Matrix3f()
        val new = renderOrientation ?: old
        val oldQuat = Quaternionf().setFromUnnormalized(old)
        val newQuat  = Quaternionf().setFromUnnormalized(new)
        oldQuat.slerp(newQuat, delta)
        return oldQuat
    }

    fun tick() {
        if (!active || orientation == null) return
        renderOrientationO = renderOrientation
        val current = orientation
        val renderMatrix = this.renderOrientation ?: current
        val renderQuat = Quaternionf().setFromUnnormalized(renderMatrix)
        val targetQuat  = Quaternionf().setFromUnnormalized(current)
        val dampingFactor = 0.66f // Smooth interpolation to reduce jitter of orientations recieved from the server.
        renderQuat.slerp(targetQuat, dampingFactor)

        val newRenderOrientation = Matrix3f()
        renderQuat.get(newRenderOrientation)
        renderOrientation = newRenderOrientation
    }

    val forwardVector: Vector3f
        get() = orientation?.transform(FORWARDS, Vector3f()) ?: Vector3f(FORWARDS)

    val leftVector: Vector3f
        get() = orientation?.transform(LEFT, Vector3f()) ?: Vector3f(LEFT)

    val upVector : Vector3f
        get() = orientation?.transform(UP, Vector3f()) ?: Vector3f(UP)

    fun rotateYaw(yaw: Float) = updateOrientation { it.rotateY(-yaw.toRadians()) }
    fun rotatePitch(pitch: Float) = updateOrientation { it.rotateX(-pitch.toRadians()) }
    fun rotateRoll(roll: Float) = updateOrientation { it.rotateZ(-roll.toRadians()) }

    val yaw: Float
        get() = Mth.wrapDegrees(-FORWARDS.angleSigned(forwardVector, UP).toDegrees() + 180)

    val pitch: Float
        get() = Mth.wrapDegrees(-asin(forwardVector.y).toDegrees())

    val roll: Float
        get() = Mth.wrapDegrees(-upVector.angleSigned(UP, forwardVector).toDegrees())

    fun applyGlobalYaw(deltaYawDegrees: Float) = updateOrientation { original ->
        return@updateOrientation original.rotateLocalY(-deltaYawDegrees.toRadians())
    }

    fun applyGlobalPitch(deltaPitchDegrees: Float) = updateOrientation { original ->
        val currQuat = Quaternionf().setFromUnnormalized(original)
        val horzLeftVector = Vector3f(0.0f, -abs(this.upVector.y.sign), 0.0f).cross(this.forwardVector)
        // Avoid NaN issue when normalizing and the forwardVector and upVector are equal
        val pitchAxis = if (horzLeftVector.lengthSquared() < 0.01) this.leftVector else horzLeftVector

        val globalPitch = Quaternionf().fromAxisAngleRad(pitchAxis.normalize(), -deltaPitchDegrees.toRadians())
        val resultQuat = globalPitch.mul(currQuat)
        return@updateOrientation Matrix3f().set(resultQuat)
    }

}
