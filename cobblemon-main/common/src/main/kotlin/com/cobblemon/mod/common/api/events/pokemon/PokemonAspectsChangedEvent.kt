/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.getPlayer
import java.util.*

data class PokemonAspectsChangedEvent(
    val ownerId: UUID?,
    val pokemon: Pokemon,
) {
    val context = mutableMapOf(
        "player" to (ownerId?.getPlayer()?.asMoLangValue() ?: DoubleValue.ZERO),
        "pokemon" to pokemon.struct
    )
}