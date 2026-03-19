/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.molang.MoLangFunctions.moLangFunctionMap
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Event that is fired when a player owned Pokémon has its happiness changed
 *
 * @author Blue
 * @since 2022-02-08
 */
data class FriendshipUpdatedEvent(
    val pokemon: Pokemon,
    val newFriendshipInitial: Int
) {
    var newFriendship: Int = newFriendshipInitial
        set(value) {
            field = value.coerceIn(0, Cobblemon.config.maxPokemonFriendship)
        }

    val context = mutableMapOf(
        "pokemon" to pokemon.struct,
        "new_friendship" to DoubleValue(newFriendship.toDouble())
    )

    val functions = moLangFunctionMap(
        "set_new_friendship" to {
            newFriendship = it.getInt(0)
            DoubleValue.ONE
        }
    )
}