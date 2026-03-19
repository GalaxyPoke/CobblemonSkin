/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.math

import com.cobblemon.mod.common.util.math.geometry.toDegrees
import kotlin.math.cos
import kotlin.math.sin
import org.joml.Quaternionf
import org.joml.Vector3f

fun Quaternionf.fromEulerXYZDegrees(vector: Vector3f): Quaternionf {
    return fromEulerXYZ(Math.toRadians(vector.x.toDouble()).toFloat(), Math.toRadians(vector.y.toDouble()).toFloat(), Math.toRadians(vector.z.toDouble()).toFloat())
}

fun Quaternionf.fromEulerXYZ(x: Float, y: Float, z: Float): Quaternionf {
    this.hamiltonProduct(Quaternionf(sin(x / 2F), 0F, 0F, cos(x / 2F)))
    this.hamiltonProduct(Quaternionf(0F, sin(y / 2F), 0F, cos(y / 2F)))
    this.hamiltonProduct(Quaternionf(0F, 0F, sin(z / 2F), cos(z / 2F)))
    return this
}

fun Quaternionf.hamiltonProduct(other: Quaternionf): Quaternionf {
    val f = this.x
    val g = this.y
    val h = this.z
    val i = this.w
    val j = other.x
    val k = other.y
    val l = other.z
    val m = other.w
    this.x = i * j + f * m + g * l - h * k
    this.y = i * k - f * l + g * m + h * j
    this.z = i * l + f * k - g * j + h * m
    this.w = i * m - f * j - g * k - h * l
    return this
}

fun Quaternionf.toEulerXYZ(): Vector3f {
   return getEulerAnglesXYZ(Vector3f(0F, 0F, 0F))
}

fun Quaternionf.toEulerXYZDegrees(): Vector3f {
    val vector = toEulerXYZ()

    vector.x = vector.x.toDegrees()
    vector.y = vector.y.toDegrees()
    vector.z = vector.z.toDegrees()

    return vector
}