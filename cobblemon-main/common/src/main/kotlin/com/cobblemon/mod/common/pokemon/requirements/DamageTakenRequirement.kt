/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.requirements

import com.cobblemon.mod.common.api.pokemon.requirement.Requirement
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.progress.DamageTakenEvolutionProgress

/**
 * An [Requirement] which requires a specific [amount] of damage taken in battle without fainting in order to pass.
 * It keeps track of progress through [DamageTakenRequirement].
 *
 * @param amount The requirement amount of damage.
 *
 * @author Licious
 * @since January 27th, 2022
 */
class DamageTakenRequirement(amount: Int) : Requirement {

    constructor() : this(0)

    /**
     * The requirement amount of damage.
     */
    val amount: Int = amount

    override fun check(pokemon: Pokemon): Boolean = pokemon.evolutionProxy.current()
        .progress()
        .filterIsInstance<DamageTakenEvolutionProgress>()
        .any { progress -> progress.currentProgress().amount >= this.amount }

    companion object {
        const val ADAPTER_VARIANT = "damage_taken"
    }

}