/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.behaviour

import com.cobblemon.mod.common.api.net.Decodable
import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.api.riding.stats.RidingStat
import net.minecraft.resources.ResourceLocation

/**
 * Represents static settings of a riding behaviour.
 * Values in this class are intended to be constant and not change during the riding process.
 * Typically this will be initialized for each pokemon form during deserialization
 * to determine how they should ride.
 *
 * These also exist in a datapacked folder, ride_settings, which are used for the fallback
 * values if non-stat settings are omitted in the pokemon form JSON.
 *
 * @author landonjw
 */
interface RidingBehaviourSettings: Encodable, Decodable {
    val key: ResourceLocation
    val stats: MutableMap<RidingStat, IntRange>

    fun calculate(stat: RidingStat, boostAmount: Float): Float {
        val range = stats[stat] ?: return 0F
        return (range.first + boostAmount).coerceAtMost(range.endInclusive.toFloat())
    }
}
