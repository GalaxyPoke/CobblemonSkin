/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.moLangFunctionMap
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.getPlayer
import net.minecraft.server.level.ServerPlayer

interface HatchEggEvent {
    val player: ServerPlayer

    data class Pre(var egg : PokemonProperties, override val player: ServerPlayer) : HatchEggEvent, Cancelable() {
        val context = mutableMapOf(
            "player" to (player.uuid.getPlayer()?.asMoLangValue() ?: DoubleValue.ZERO)
        )
        val functions = moLangFunctionMap(
            cancelFunc
        )
    }

    data class Post(override var player: ServerPlayer, val pokemon : Pokemon) : HatchEggEvent {
        val context = mutableMapOf(
            "player" to (player.uuid.getPlayer()?.asMoLangValue() ?: DoubleValue.ZERO)
        )
    }
}
