/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.cooking

import net.minecraft.world.effect.MobEffectInstance
import kotlin.math.ceil
import kotlin.math.max

object MobEffectUtils {
    fun mergeEffects(effects: List<SerializableMobEffectInstance>): List<SerializableMobEffectInstance> {
        val grouped = effects.groupBy { it.effect }

        return grouped.map { (effectId, group) ->
            val amplifier = mergeAmplifiers(group.map { it.amplifier })
            val duration = mergeDurations(group.map { it.duration })

            val first = group.first()
            SerializableMobEffectInstance(
                    effect = effectId,
                    duration = duration,
                    amplifier = amplifier,
                    ambient = first.ambient,
                    visible = first.visible,
                    showIcon = first.showIcon
            )
        }
    }

    private fun mergeDurations(durations: List<Int>): Int {
        if (durations.isEmpty()) return 0

        val sorted = durations.sortedDescending()
        var result = 0.0

        for ((index, value) in sorted.withIndex()) {
            val multiplier = when (index) {
                0 -> 1.0
                1 -> 0.75
                2 -> 0.5
                else -> 0.25
            }
            result += value * multiplier
        }

        return ceil(result).toInt()
    }

    private fun mergeAmplifiers(amplifiers: List<Int>): Int {
        return amplifiers.maxOrNull() ?: 0 // we do not want the power of the effects to combine, just take the highest one used of that type
    }
}