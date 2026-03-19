/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.util.toProperties

/**
 * A definition for a Pokémon that this Pokémon can tolerate as a herd leader. Pokémon identified
 * by this will be followed by the species this setting is on.
 *
 * @author Hiroku
 * @since June 26th, 2025
 */
class ToleratedHerdLeader(
    var pokemon: PokemonProperties,
    /** A higher tier indicates a more favourable herd leader. */
    val tier: Int,
    /** If true, a lower level Pokémon can still lead it.*/
    val ignoresLevel: Boolean = false,
    /** In case some leaders require a different follow distance? */
    val followDistance: IntRange? = null
) {
    fun initialize() {
        // First time this is deserialized, there is nothing in Species registry.
        pokemon = pokemon.originalString.toProperties()
    }
}