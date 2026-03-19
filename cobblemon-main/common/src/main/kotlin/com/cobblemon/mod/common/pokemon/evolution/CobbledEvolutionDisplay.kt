/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution

import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionDisplay
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species

internal data class CobblemonEvolutionDisplay(
    override val id: String,
    override val species: Species,
    override val aspects: Set<String>
) : EvolutionDisplay {

    constructor(id: String, pokemon: Pokemon) : this(id, pokemon.species, pokemon.aspects)

}