/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.evolution

import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.evolution.StandardPreEvolution

/**
 * Represents the previous stage in the evolutionary line of a given Pokémon.
 * Not all species will have one.
 *
 * @author Licious
 * @since March 22nd, 2022
 */
interface PreEvolution {

    val species: Species

    val form: FormData

    companion object {

        fun of(species: Species, form: FormData = species.standardForm): PreEvolution = StandardPreEvolution(species, form)

    }

}