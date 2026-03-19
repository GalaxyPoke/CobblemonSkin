/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.cobblemon.mod.common.util.getBooleanOrNull
import com.cobblemon.mod.common.util.getDoubleOrNull
import kotlin.math.sqrt
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.PathfinderMob

/**
 * Used to maintain some settings around how the entity should wander this tick. The idea is you
 * reset this with some script task at priority 0 then some number of steps before it gets to the wander
 * tasks you can have other conditional tasks jump in to configure parts of this.
 *
 * @author Hiroku
 * @since July 18, 2025
 */
class CobblemonWanderControl(
    var center: WanderCenter? = null,
    var maxAttempts: Int = 4, // How many times to try to find a suitable point before giving up on that tick.
    var allowLand: Boolean = true,
    var allowWater: Boolean = false,
    var allowAir: Boolean = false,
    var pathCooldownTicks: Int = 20 * 5, // Used to mitigate the cost of pathfinding.
    var wanderSpeed: Float = 0.35F,
) {
    class WanderCenter(val x: Number, val y: Number, val z: Number, val minRange: Float, val maxRange: Float)

    @Transient
    val struct: QueryStruct = QueryStruct(hashMapOf())
        .addFunction("center_x") { center?.x ?: 0.0 }
        .addFunction("center_y") { center?.y ?: 0.0 }
        .addFunction("center_z") { center?.z ?: 0.0 }
        .addFunction("max_attempts") { maxAttempts.toDouble() }
        .addFunction("min_range") { center?.minRange ?: 0.0 }
        .addFunction("max_range") { center?.maxRange ?: 0.0 }
        .addFunction("allow_land") { if (allowLand) 1.0 else 0.0 }
        .addFunction("allow_water") { if (allowWater) 1.0 else 0.0 }
        .addFunction("allow_air") { if (allowAir) 1.0 else 0.0 }
        .addFunction("path_cooldown_ticks") { pathCooldownTicks.toDouble() }
        .addFunction("wander_speed") { wanderSpeed }
        .addFunction("set_center") { params ->
            if (params.params.size < 3) {
                throw IllegalArgumentException("set_center requires at least 3 parameters: x, y, z")
            }
            val x = params.getDouble(0)
            val y = params.getDouble(1)
            val z = params.getDouble(2)
            val minRange = params.getDoubleOrNull(3) ?: 0.0
            val maxRange = params.getDoubleOrNull(4) ?: Float.MAX_VALUE.toDouble()
            center = WanderCenter(
                x = x,
                y = y,
                z = z,
                minRange = minRange.toFloat(),
                maxRange = maxRange.toFloat(),
            )
        }
        .addFunction("set_max_attempts") { params -> maxAttempts = params.getDoubleOrNull(0)?.toInt() ?: 4 }
        .addFunction("set_allow_land") { params -> allowLand = params.getBooleanOrNull(0) ?: true }
        .addFunction("set_allow_water") { params -> allowWater = params.getBooleanOrNull(0) ?: false }
        .addFunction("set_allow_air") { params -> allowAir = params.getBooleanOrNull(0) ?: false }
        .addFunction("set_path_cooldown_ticks") { params -> pathCooldownTicks = params.getDoubleOrNull(0)?.toInt() ?: (20 * 5) }
        .addFunction("set_wander_speed") { params -> wanderSpeed = params.getDoubleOrNull(0)?.toFloat() ?: 0.35F }
        .addFunction("reset") { reset() }

    fun isSuitable(position: BlockPos): Boolean {
        val center = center ?: return true
        val centerPos = BlockPos(center.x.toInt(), center.y.toInt(), center.z.toInt())
        val distance = sqrt(centerPos.distSqr(position))
        val suitable = distance >= center.minRange && distance <= center.maxRange
        return suitable
    }

    fun reset() {
        center = null
        allowLand = false
        allowWater = false
        allowAir = false
        pathCooldownTicks = 20 * 5
        wanderSpeed = 0.35F
        maxAttempts = 4
    }
}

