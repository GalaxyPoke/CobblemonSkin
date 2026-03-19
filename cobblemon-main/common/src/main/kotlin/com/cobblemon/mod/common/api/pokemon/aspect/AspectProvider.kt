/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.aspect

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * A provider of 'aspects'. An aspect is a trait of a Pokémon that may be used
 * to decide visual characteristics. This interface is the mechanism used to calculate
 * the aspects of a specific [Pokemon] or [PokemonProperties].
 *
 * @author Hiroku
 * @since May 13th, 2022
 */
interface AspectProvider {
    companion object {
        val providers = mutableListOf<AspectProvider>()
        fun register(provider: AspectProvider): AspectProvider {
            providers.add(provider)
            return provider
        }
        fun unregister(provider: AspectProvider) {
            providers.remove(provider)
        }
    }

    /** Returns a set of aspects for this [Pokemon]. It's fine if this is empty. */
    fun provide(pokemon: Pokemon): Set<String>
    /** Returns a set of aspects for this [PokemonProperties]. It's fine if this is empty. */
    fun provide(properties: PokemonProperties): Set<String>

    /** Just a convenience function. */
    fun register(): AspectProvider {
        return register(this)
    }
}