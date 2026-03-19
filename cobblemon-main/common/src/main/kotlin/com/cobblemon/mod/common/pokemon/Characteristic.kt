/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.util.collections.RotatedIterable
import java.util.UUID
import kotlin.math.abs

const val CHARACTERISTIC_MODULUS: Int = 5

data class Characteristic(val relevantStat: Stat, val mod: Int) {
    companion object {
        fun calculate(ivs: IVs, uuid: UUID): Characteristic {
            val ivList = ivs.toList()
            // If multiple IVs are the highest maxWithOrNull always returns the first one found, so we rotate the first one
            // found to depend on UUID (vis-à-vis Personality value)
            val startAt = abs(uuid.hashCode()) % ivList.size
            val relevantIv = RotatedIterable(ivList, startAt).maxByOrNull { it.value } ?: return empty()
            val mod = relevantIv.value % CHARACTERISTIC_MODULUS
            return Characteristic(relevantIv.key, mod)
        }

        // Provide an empty default Characteristic for the extremely rare cases that a Pokémon has no proper IVs
        fun empty(): Characteristic = Characteristic(Stats.HP, 0)
    }
}
