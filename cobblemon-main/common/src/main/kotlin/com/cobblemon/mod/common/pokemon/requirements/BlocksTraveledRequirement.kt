/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.requirements

import com.cobblemon.mod.common.api.pokemon.requirement.Requirement
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * An [Requirement] that requires a specific [amount] of [PokemonEntity.blocksTraveled] to pass.
 *
 * @param amount The amount of blocks the entity must have traversed.
 *
 * @author Licious
 * @since January 28th, 2023
 */
class BlocksTraveledRequirement(amount: Int) : Requirement {

    constructor() : this(0)

    val amount: Int = amount

    override fun check(pokemon: Pokemon): Boolean {
        return pokemon.getBlocksTraveled() >= this.amount
    }

    companion object {
        const val ADAPTER_VARIANT = "blocks_traveled"
    }

}
