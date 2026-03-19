/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon

import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.moLangFunctionMap
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Event fired when a [PokemonEntity] is recalled.
 *
 * @author Segfault Guy
 * @since March 25th, 2023
 */
interface PokemonRecallEvent {

    val pokemon: Pokemon
    val oldEntity: PokemonEntity?

    /**
     * Event fired before a [PokemonEntity] is recalled.
     */
    data class Pre(
        override val pokemon: Pokemon,
        override val oldEntity: PokemonEntity?
    ) : PokemonRecallEvent, Cancelable() {

        val context = mutableMapOf(
            "pokemon" to pokemon.struct,
            "old_entity" to (oldEntity?.struct ?: StringValue("null"))
        )

        val functions = moLangFunctionMap(
            cancelFunc
        )
    }

    data class Post(
        override val pokemon: Pokemon,
        override val oldEntity: PokemonEntity?
    ) : PokemonRecallEvent {
        val context = mutableMapOf(
            "pokemon" to pokemon.struct,
            "old_entity" to (oldEntity?.struct ?: StringValue("null"))
        )
    }
}