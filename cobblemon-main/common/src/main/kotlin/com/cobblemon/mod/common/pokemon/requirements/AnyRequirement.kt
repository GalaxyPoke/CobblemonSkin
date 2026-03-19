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

/**
 * An [Requirement] that succeeds when any of the [possibilities] are valid.
 *
 * @property possibilities A collection of possible [Requirement]s that can allow this requirement to be valid.
 */
class AnyRequirement(val possibilities: Collection<Requirement>) : Requirement {
    override fun check(pokemon: Pokemon) = this.possibilities.any { it.check(pokemon) }
    companion object {
        const val ADAPTER_VARIANT = "any"
    }
}