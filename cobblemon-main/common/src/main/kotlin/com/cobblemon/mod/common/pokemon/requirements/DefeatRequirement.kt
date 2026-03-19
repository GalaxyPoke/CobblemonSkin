/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.requirements

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.requirement.Requirement
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.progress.DefeatEvolutionProgress

/**
 * An [Requirement] which requires a specific [amount] of [target]s defeated to pass.
 *
 * @param target The [PokemonProperties] of the target Pokémon.
 * @param amount The amount of [target]s to faint.
 *
 * @author Licious
 * @since January 28th, 2022
 */
class DefeatRequirement(target: PokemonProperties, amount: Int) : Requirement {

    constructor() : this(PokemonProperties(), 0)

    val target: PokemonProperties = target
    val amount: Int = amount

    override fun check(pokemon: Pokemon): Boolean = pokemon.evolutionProxy.current()
        .progress()
        .filterIsInstance<DefeatEvolutionProgress>()
        .any { progress -> progress.currentProgress().target.originalString.equals(this.target.originalString, true) && progress.currentProgress().amount >= this.amount }

    companion object {
        const val ADAPTER_VARIANT = "defeat"
    }

}