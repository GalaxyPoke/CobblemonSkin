/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokedex.scanner

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species

// Hiro note: I honestly want to move to not using this at all. There is always a Pokemon behind the data,
// it's just that sometimes it's harder to reach or there's a Zoroark or Ditto throwing a curveball. It makes
// in shittier in events to have to cater to two distinct classes of data that can be scanned.

data class PokedexEntityData(
    val pokemon: Pokemon,
    val disguise: DisguiseData?
) {
    class DisguiseData(
        val species: Species,
        val form: FormData,
    ) {
        val struct = QueryStruct(hashMapOf())
            .addFunction("species") { species.struct }
            .addFunction("form") { StringValue(form.name) }
    }

    fun getApparentSpecies() = disguise?.species ?: pokemon.species
    fun getApparentForm() = disguise?.form ?: pokemon.form
}