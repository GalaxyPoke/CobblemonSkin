/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon.evolution

import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * The root of the various evolution related events.
 */
interface EvolutionEvent {

    /**
     * The Pokémon impacted in this event.
     */
    val pokemon: Pokemon

    /**
     * The evolution triggering this event.
     */
    val evolution: Evolution

}