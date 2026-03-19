/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.cooking

import com.cobblemon.mod.common.item.components.FoodComponent
import kotlin.math.ceil
import kotlin.math.round

object FoodUtils {
    fun merge(components: List<FoodComponent>, extraHunger: Int = 0, extraSaturation: Float = 0f): FoodComponent {
        if (components.isEmpty()) return FoodComponent(0, 0f)

        val count = components.size
        val totalHunger = components.sumOf { it.hunger.toDouble() } + extraHunger
        val totalSaturation = components.sumOf { it.saturation.toDouble() } + extraSaturation

        val multiplier = when (count) {
            1 -> 1.0
            2 -> 0.8
            3 -> 0.6
            else -> 0.4
        }

        val adjustedHunger = ceil(totalHunger * multiplier)
        val adjustedSaturation = totalSaturation * multiplier

        return FoodComponent(
                hunger = adjustedHunger.toInt(),
                saturation = (round(adjustedSaturation * 100) / 100f).toFloat()
        )
    }
}