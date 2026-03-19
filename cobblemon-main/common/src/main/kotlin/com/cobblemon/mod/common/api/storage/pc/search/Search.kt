/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.pc.search

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.pokemon.Pokemon
import java.util.HashSet
import java.util.Locale
import java.util.UUID

/**
 * Client-side class containing currently applied filters, and cached results of which Pokémon pass or fail those filters.
 * Used in [com.cobblemon.mod.common.client.gui.pc.PCGUI]
 */
class Search(
    private val filters: Set<PokemonFilter>,
    private val passed: MutableSet<UUID> = HashSet(),
    private val failed: MutableSet<UUID> = HashSet()
) {
    companion object {
        val DEFAULT = Search(setOf())
        fun of(search: String): Search {
            if (search.isBlank()) return DEFAULT

            val split = search.lowercase(Locale.ROOT).trim().split(" ")
            val filters = HashSet<PokemonFilter>()

            for (piece in split) {
                var filter = piece
                val inverted = filter.startsWith("!")
                if (inverted) {
                    filter = filter.substring(1)
                }

                filter = filter.lowercase(Locale.ROOT)

                val pokemonFilter: PokemonFilter = when (filter) {
                    "holding", "helditem", "held_item" -> PokemonFilter { pokemon -> !pokemon.heldItem().isEmpty }
                    "fainted" -> PokemonFilter { pokemon -> pokemon.isFainted() }
                    "legendary" -> PokemonFilter { pokemon -> pokemon.isLegendary() }
                    "mythical" -> PokemonFilter { pokemon -> pokemon.isMythical() }
                    "ultrabeast", "ultra_beast" -> PokemonFilter { pokemon -> pokemon.isUltraBeast() }
                    else -> {
                        PokemonFilter { pokemon ->
                            if (filter.isEmpty()) {
                                true
                            } else {
                                val species = pokemon.species.translatedName.string.lowercase(Locale.ROOT)
                                val name = pokemon.getDisplayName().string.lowercase(Locale.ROOT)
                                val props = PokemonProperties.parse(filter)

                                species.contains(filter) || name.contains(filter) || (props.matches(pokemon) && props.asString().isNotEmpty())
                            }
                        }
                    }
                }

                if (inverted) {
                    filters.add(pokemonFilter.inverted())
                } else {
                    filters.add(pokemonFilter)
                }
            }

            return Search(filters)
        }
    }

    fun passes(pokemon: Pokemon?): Boolean {
        if (pokemon == null) return false
        val uuid = pokemon.uuid
        if (passed.contains(uuid)) return true
        if (failed.contains(uuid)) return false
        val passes = filters.all { it.test(pokemon) }
        val set = if (passes) passed else failed
        set.add(uuid)
        return passes
    }
}