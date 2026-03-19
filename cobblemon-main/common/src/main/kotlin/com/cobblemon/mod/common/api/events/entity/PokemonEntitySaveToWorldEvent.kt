/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.entity

import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity

/**
 * Event fired when a Pokémon is about to be saved to the world. Cancel this event if the Pokémon
 * should not be saved to the world.
 *
 * @author Hiroku
 * @since January 7th, 2022
 */
class PokemonEntitySaveToWorldEvent(val pokemonEntity: PokemonEntity) : Cancelable()