/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.properties

import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * A type of [CustomPokemonProperty] which is a simple label representing the type.
 *
 * An instance of this property is either in add-mode or remove-mode. When in remove mode,
 * applying it to a Pokémon will remove the flag if it exists on that Pokémon.
 *
 * @author Hiroku
 * @since July 1st, 2022
 */
class FlagProperty(val key: String, val remove: Boolean = false) : CustomPokemonProperty {
    override fun asString() = key
    override fun apply(pokemon: Pokemon) {
        if (remove) {
            pokemon.customProperties.removeIf { it is FlagProperty && it.key == key }
        } else {
            pokemon.customProperties.add(this)
        }
    }
    override fun matches(pokemon: Pokemon) = pokemon.customProperties.any { it is FlagProperty && it.key == key }
}