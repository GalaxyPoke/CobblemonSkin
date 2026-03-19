/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.aspect

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * A specific type of [AspectProvider] which, upon satisfying some condition,
 * returns a single aspect. This is just a convenient interface for common usages.
 *
 * @author Hiroku
 * @since May 13th, 2022
 */
interface SingleConditionalAspectProvider : AspectProvider {
    companion object {
        fun getForFeature(name: String): SingleConditionalAspectProvider {
            return object : SingleConditionalAspectProvider {
                override val aspect: String = name
                override fun meetsCondition(pokemon: Pokemon) = pokemon.getFeature<FlagSpeciesFeature>(name)?.enabled == true
                override fun meetsCondition(pokemonProperties: PokemonProperties) = pokemonProperties
                    .customProperties
                    .filterIsInstance<FlagSpeciesFeature>()
                    .any { it.name == name && it.enabled }

            }
        }
    }

    /** The aspect to add if the conditions are met. */
    val aspect: String
    fun meetsCondition(pokemon: Pokemon): Boolean
    fun meetsCondition(pokemonProperties: PokemonProperties): Boolean

    override fun provide(properties: PokemonProperties): Set<String> {
        return if (meetsCondition(properties)) {
            setOf(aspect)
        } else {
            emptySet()
        }
    }

    override fun provide(pokemon: Pokemon): Set<String> {
        return if (meetsCondition(pokemon)) {
            setOf(aspect)
        } else {
            emptySet()
        }
    }
}