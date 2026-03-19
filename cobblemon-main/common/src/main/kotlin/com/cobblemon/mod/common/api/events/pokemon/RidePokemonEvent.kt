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
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerPlayer

/**
 * The base for all events related to riding a Pokemon.
 *
 * @see [Pre]
 * @see [Post]
 */
interface RidePokemonEvent {
    val player: ServerPlayer
    val pokemon: PokemonEntity

    class Pre(
        override val player: ServerPlayer,
        override val pokemon: PokemonEntity
    ) : RidePokemonEvent, Cancelable() {
        val context = mutableMapOf(
            "player" to player.asMoLangValue(),
            "pokemon" to pokemon.struct
        )
        val functions = moLangFunctionMap(
            cancelFunc
        )
    }

    class Post(
        override val player: ServerPlayer,
        override val pokemon: PokemonEntity
    ) : RidePokemonEvent {
        val context = mutableMapOf(
            "player" to player.asMoLangValue(),
            "pokemon" to pokemon.struct
        )
    }

    /**
     * Event published when stamina is being applied. At this point you can modify the stamina that will get used on the
     * mount or you can make the stamina infinite. Handy hey.
     */
    class ApplyStamina(
        override val player: ServerPlayer,
        override val pokemon: PokemonEntity,
        var rideStamina: Float
    ) : RidePokemonEvent {
        val context = mutableMapOf(
            "player" to player.asMoLangValue(),
            "pokemon" to pokemon.struct,
            "ride_stamina" to DoubleValue(rideStamina)
        )
        val functions = moLangFunctionMap(
            "set_ride_stamina" to { params ->
                rideStamina = params.getDouble(0).toFloat().coerceIn(0F, 1F)
                DoubleValue.ONE
            },
            "set_infinite_stamina" to { _ ->
                rideStamina = -1F
                DoubleValue.ONE
            },
        )

        fun setInfiniteStamina() {
            rideStamina = -1F
        }

        fun isInfiniteStamina() = rideStamina == -1F
    }
}