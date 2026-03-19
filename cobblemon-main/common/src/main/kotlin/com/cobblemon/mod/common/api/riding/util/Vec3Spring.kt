/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.util

import net.minecraft.world.phys.Vec3
import kotlin.math.min

/**
 * A spring-based smoother for Vec3 values, designed for applying damped spring motion to riding data.
 * Stores the previous and current values, along with a history buffer for lookback-based interpolation.
 *
 * @author Jackowes
 * @since March 27th, 2025
 */


class Vec3Spring(
    var value: Vec3 = Vec3.ZERO,
    var velocity: Vec3 = Vec3.ZERO
) {
    val history = ArrayDeque<Vec3>()
    //Hold up to a seconds worth of values
    val lookBackSize = 20

    fun update(target: Vec3, stiffness: Double, damping: Double) {

        //Hooke's Law f = kx where:
        //f is force
        //k is the spring constant or stiffness of this spring
        //x is the distance from rest and in this case the difference between the most recent
        //rotation delta ( the target ) and the springs current value for delta rot
        val force = target.subtract(value).scale(stiffness)

        //Now calculate acceleration and apply damping
        val accel = force.subtract(velocity.scale(damping))

        velocity = velocity.add(accel.scale(1.0/20.0))
        value = value.add(velocity.scale(1.0/20.0))

        history.addFirst(value)
        //Adding one since if we want to check max look back point it needs something to interp with.
        if( history.size > lookBackSize + 1) {
            history.removeLast()
        }
    }

    fun getInterpolated(partialTick: Double, lookBackTick: Int): Vec3 {
        if (history.size < 2) {
            return value // or Vec3.ZERO if you prefer
        }

        val tick = min(lookBackTick, history.size - 2)
        var ret = Vec3.ZERO
        for( i in 0..tick) {
            val older = history.get(i + 1).scale(1.0)
            val newer = history.get(i).scale(1.0)
            ret = ret.add(older.add(newer.subtract(older).scale(partialTick)))
        }

        return ret.scale(1.0/(tick+1))
    }
}