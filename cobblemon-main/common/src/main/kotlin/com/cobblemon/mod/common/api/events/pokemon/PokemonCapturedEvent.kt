/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon

import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.itemRegistry
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerPlayer

data class PokemonCapturedEvent (
    val pokemon: Pokemon,
    val player: ServerPlayer,
    val pokeBallEntity: EmptyPokeBallEntity
) {
    val context = mutableMapOf<String, MoValue>(
        "pokemon" to pokemon.struct,
        "player" to player.asMoLangValue(),
        "poke_ball" to pokeBallEntity.struct,
        "item" to player.level().itemRegistry.wrapAsHolder(pokeBallEntity.pokeBall.item).asMoLangValue(Registries.ITEM)
    )
}