/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon.evolution

import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.pokemon.evolution.EvolutionDisplay
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Fired before an evolution is sent to clients.
 * Any modification made to the [pokemon] will reflect in their display.
 * For modifications on the actual output when they accept it modify the [Evolution.result].
 *
 * @param pokemon The [Pokemon] that will evolve if this proposition is accepted.
 * @param display The current [EvolutionDisplay] the client will see.
 * @param evolution The [Evolution] being used.
 *
 * @author Licious
 * @since May 3rd, 2022
 */
data class EvolutionDisplayEvent(
    override val pokemon: Pokemon,
    var display: EvolutionDisplay,
    override val evolution: Evolution
) : EvolutionEvent