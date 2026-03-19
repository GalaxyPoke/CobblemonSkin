/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.math

import kotlin.math.floor

class CatmullRomCurve(val nodes: List<Double>) {
    fun getY(t: Double): Double {
        val points = nodes

        val p = (points.size - 1) * t
        val intPoint = floor(p).toInt()

        val weight = p - intPoint

        val p0 = points[if (intPoint <= 0) 0 else intPoint - 1]
        val p1 = points[if (intPoint < 0) 0 else intPoint]
        val p2 = points[if (intPoint > points.size - 2) points.size - 1 else intPoint + 1]
        val p3 = points[if (intPoint > points.size - 3) points.size - 1 else intPoint + 2]

        return catmullRom(weight, p0, p1, p2, p3)
    }
}

fun catmullRom(t: Double, p0: Double, p1: Double, p2: Double, p3: Double): Double {
    val v0 = (p2 - p0) * 0.5
    val v1 = (p3 - p1) * 0.5
    val t2 = t * t
    val t3 = t * t2

    return (2 * p1 - 2 * p2 + v0 + v1) * t3 + (-3 * p1 + 3 * p2 - 2 * v0 - v1) * t2 + v0 * t + p1
}

fun quadraticBezierP0(t: Double, p: Double): Double {
    val k = 1 - t
    return k * k * p
}

fun quadraticBezierP1(t: Double, p: Double): Double {
    return 2 * (1 - t) * t * p
}

fun quadraticBezierP2(t: Double, p: Double): Double {
    return t * t * p
}

fun quadraticBezier(t: Double, p0: Double, p1: Double, p2: Double): Double {
    return quadraticBezierP0(t, p0) + quadraticBezierP1(t, p1) + quadraticBezierP2(t, p2)
}

fun cubicBezierP0(t: Double, p: Double): Double {
    val k = 1 - t
    return k * k * k * p
}

fun cubicBezierP1(t: Double, p: Double): Double {
    val k = 1 - t
    return 3 * k * k * t * p
}

fun cubicBezierP2(t: Double, p: Double): Double {
    return 3 * (1 - t) * t * t * p
}

fun cubicBezierP3(t: Double, p: Double): Double {
    return t * t * t * p
}

class CubedBezierCurve(
    val v0: Double,
    val v1: Double,
    val v2: Double,
    val v3: Double
) {
    fun getY(t: Double): Double {
        return cubicBezierP0(t, v0) + cubicBezierP1(t, v1) +cubicBezierP2(t, v2) +cubicBezierP3(t, v3)
    }
}