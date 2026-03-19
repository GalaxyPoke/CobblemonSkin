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

class CollectEggEvent (
    val egg : PokemonProperties,
    val maleParent : Pokemon,
    val femaleParent : Pokemon,
    val player : ServerPlayer
) : Cancelable() {
    val context = mutableMapOf(
        "male_parent" to maleParent.struct,
        "female_parent" to femaleParent.struct,
        "player" to (player.uuid.getPlayer()?.asMoLangValue() ?: DoubleValue.ZERO)
    )
    val functions = moLangFunctionMap(
        cancelFunc
    )
}
