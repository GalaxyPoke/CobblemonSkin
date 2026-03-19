/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.preset

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail

/**
 * A [SpawnDetailPreset] that has extra fields that can apply specifically to [PokemonSpawnDetail]s.
 *
 * @author Hiroku
 * @since July 8th, 2022
 */
class PokemonSpawnDetailPreset : SpawnDetailPreset() {
    companion object {
        const val NAME = "pokemon"
    }

    var pokemon: PokemonProperties? = null
    var levelRange: IntRange? = null

    override fun apply(spawnDetail: SpawnDetail) {
        super.apply(spawnDetail)
        if (spawnDetail is PokemonSpawnDetail) {
            val pokemon = pokemon
            if (pokemon != null) {
                spawnDetail.pokemon = PokemonProperties.parse(spawnDetail.pokemon.originalString + " " + pokemon.originalString)
            }
            if (levelRange != null) {
                spawnDetail.levelRange = levelRange
            }
        }
    }
}