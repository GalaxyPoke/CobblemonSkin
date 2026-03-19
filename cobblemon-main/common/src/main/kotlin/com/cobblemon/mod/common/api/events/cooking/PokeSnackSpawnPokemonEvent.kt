/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.cooking

import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.moLangFunctionMap
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.block.entity.PokeSnackBlockEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity

interface PokeSnackSpawnPokemonEvent {

    data class Pre(
        val pokeSnackBlockEntity: PokeSnackBlockEntity,
        val spawnAction: SpawnAction<*>,
    ) : Cancelable(), PokeSnackSpawnPokemonEvent {
        val context = mapOf<String, MoValue>()
        val functions = moLangFunctionMap(
            cancelFunc
        )
    }

    data class Post(
        val pokeSnackBlockEntity: PokeSnackBlockEntity,
        val spawnAction: SpawnAction<*>,
        val pokemonEntity: PokemonEntity,
    ) : PokeSnackSpawnPokemonEvent {
        val context = mapOf<String, MoValue>()
    }

}