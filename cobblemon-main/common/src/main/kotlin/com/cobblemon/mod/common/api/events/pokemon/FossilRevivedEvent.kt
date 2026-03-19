/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon

import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer

data class FossilRevivedEvent (
    val pokemon: Pokemon,
    val player: ServerPlayer?
) {
    val context = mutableMapOf<String, MoValue>(
        "pokemon" to pokemon.struct,
        "player" to (player?.asMoLangValue() ?: DoubleValue.ZERO)
    )
}
