/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.fishing

import kotlin.math.ceil

object SpawnBaitUtils {
    @JvmStatic
    fun mergeEffects(effects: List<SpawnBait.Effect>): List<SpawnBait.Effect> {
        val grouped = effects.groupBy { Pair(it.type, it.subcategory) }

        return grouped.map { (key, groupEffects) ->
            val (type, subcategory) = key

            val totalChance = groupEffects.sumOf { it.chance }
            val totalValue = groupEffects.sumOf { it.value }

            val adjustedChance = totalChance.coerceAtMost(1.0)
            val adjustedValue = ceil(totalValue)

            SpawnBait.Effect(type, subcategory, adjustedChance, adjustedValue)
        }
    }
}