/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.pc.search

import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Functional interface used in [Search]es
 */
fun interface PokemonFilter {
    fun test(pokemon: Pokemon): Boolean

    fun inverted(): PokemonFilter {
        return PokemonFilter { pokemon -> !this.test(pokemon) }
    }
}