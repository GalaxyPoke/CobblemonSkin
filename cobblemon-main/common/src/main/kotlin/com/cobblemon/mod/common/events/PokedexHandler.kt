/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.PokemonAspectsChangedEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonGainedEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonSeenEvent

object PokedexHandler : EventHandler {
    override fun registerListeners() {
        CobblemonEvents.POKEMON_GAINED.subscribe(Priority.NORMAL, ::onPokemonGained)
        CobblemonEvents.POKEMON_SEEN.subscribe(Priority.NORMAL, ::onPokemonSeen)
        CobblemonEvents.POKEMON_ASPECTS_CHANGED.subscribe(Priority.NORMAL, ::onPokemonAspectsChanged)
    }

    fun onPokemonGained(event: PokemonGainedEvent) {
        Cobblemon.playerDataManager.getPokedexData(event.playerId).catch(event.pokemon)
    }

    fun onPokemonSeen(event: PokemonSeenEvent) {
        Cobblemon.playerDataManager.getPokedexData(event.playerId).encounter(event.pokemon)
    }

    fun onPokemonAspectsChanged(event: PokemonAspectsChangedEvent) {
        if (event.ownerId != null) {
            Cobblemon.playerDataManager.getPokedexData(event.ownerId).catch(event.pokemon)
        }
    }
}