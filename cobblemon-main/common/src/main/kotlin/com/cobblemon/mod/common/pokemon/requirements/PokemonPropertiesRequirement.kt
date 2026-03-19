/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.requirements

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.requirement.Requirement
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * An [Requirement] for when the [Pokemon] must match [PokemonProperties.matches].
 *
 * @property target The matcher for this requirement.
 * @author Licious
 * @since March 26th, 2022
 */
class PokemonPropertiesRequirement : Requirement {
    val target = PokemonProperties()
    override fun check(pokemon: Pokemon) = this.target.matches(pokemon)

    companion object {
        const val ADAPTER_VARIANT = "properties"
    }
}