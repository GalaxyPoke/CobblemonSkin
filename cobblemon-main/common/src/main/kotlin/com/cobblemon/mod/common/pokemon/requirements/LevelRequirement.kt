/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.requirements

import com.cobblemon.mod.common.api.pokemon.requirement.Requirement
import com.cobblemon.mod.common.pokemon.Pokemon

class LevelRequirement : Requirement {
    companion object {
        const val ADAPTER_VARIANT = "level"
    }

    val minLevel = 1
    val maxLevel = Int.MAX_VALUE
    override fun check(pokemon: Pokemon) = pokemon.level in minLevel..maxLevel
}