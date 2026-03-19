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
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer

/**
 * Event that is fired when a Player mounts a Pokemon to a shoulder
 *
 * @author Qu
 * @since 2022-01-26
 */
data class ShoulderMountEvent(
    val player: ServerPlayer,
    val pokemon: Pokemon,
    val isLeft: Boolean
) : Cancelable() {
    val context = mutableMapOf(
        "player" to (player.asMoLangValue() ?: DoubleValue.ZERO),
        "pokemon" to pokemon.struct,
        "is_left" to DoubleValue(if (isLeft) 1.0 else 0.0),
    )
    val functions = moLangFunctionMap(
        cancelFunc
    )
}