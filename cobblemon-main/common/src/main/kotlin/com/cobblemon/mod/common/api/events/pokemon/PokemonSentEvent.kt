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
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

/**
 * Event fired when a party [Pokemon] is sent out.
 *
 * @author Segfault Guy
 * @since March 25th, 2023
 */
interface PokemonSentEvent {
    /**
     * The [Pokemon] being sent out.
     */
    val pokemon: Pokemon

    /**
     * The [ServerLevel] the [Pokemon] is being sent out into.
     */
    val level: ServerLevel

    /**
     * The [Vec3] position the [Pokemon] is being sent out at.
     */
    val position: Vec3

    /**
     * Event fired when a party [Pokemon] is sent out. Cancelling this event prevents a corresponding
     * [PokemonEntity] from being instantiated and spawned into the world.
     *
     * @author Segfault Guy/MeAlam
     * @since March 25th, 2023
     */
    data class Pre(
        override val pokemon: Pokemon,
        override val level: ServerLevel,
        override val position: Vec3
    ) : PokemonSentEvent, Cancelable() {
        val context = mutableMapOf(
            "pokemon" to pokemon.struct,
            "position" to StringValue(position.toString())
        )
        val functions = moLangFunctionMap(
            cancelFunc
        )
    }

    /**
     * Event fired after a [PokemonEntity] is spawned from a player's party and after its animations are finished.
     * Only fired for party [Pokemon] sent out with animations.
     *
     * @author Segfault Guy/MeAlam
     * @since March 25th, 2023
     */
    data class Post(
        override val pokemon: Pokemon,
        override val level: ServerLevel,
        override val position: Vec3,
        val pokemonEntity: PokemonEntity
    ) : PokemonSentEvent {
        val context = mutableMapOf(
            "pokemon" to pokemon.struct,
            "pokemon_entity" to pokemonEntity.struct
        )
    }
}