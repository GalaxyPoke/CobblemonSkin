/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.drop

import com.cobblemon.mod.common.api.pokemon.requirement.Requirement
import com.cobblemon.mod.common.pokemon.Pokemon

class EvolutionItemDropEntry : ItemDropEntry() {
    val requirements: MutableSet<Requirement> = mutableSetOf()

    override fun canDrop(pokemon: Pokemon?): Boolean = pokemon != null && requirements.all { it.check(pokemon) }
}