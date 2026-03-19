/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon

import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer

/**
 * An event to modify the shiny chance of a Pokémon.
 */
class ShinyChanceCalculationEvent(
    val baseChance: Float,
    val pokemon: Pokemon
) {
    val chance: Float = baseChance
    var isShiny: Boolean = false
    private val modifiers = mutableListOf<Float>()
    private val modificationFunctions = mutableListOf<(Float, ServerPlayer?, Pokemon) -> Float>()

    /**
     * Adds a modifier to the shiny chance.
     */
    fun addModifier(modifier: Float) {
        modifiers.add(modifier)
    }

    /**
     * Adds a function to modify the shiny chance.
     */
    fun addModificationFunction(function: (Float, ServerPlayer?, Pokemon) -> Float) {
        modificationFunctions.add(function)
    }

    /**
     * Calculates the shiny chance of a Pokémon.
     */
    fun calculate(player: ServerPlayer?): Float {
        var result = baseChance
        for (modifier in modifiers) {
            result += modifier
        }
        for (function in modificationFunctions) {
            result = function(result, player, pokemon)
        }
        return result
    }

    fun isShiny(player: ServerPlayer?): Boolean {
        return calculate(player) >= chance || isShiny
    }
}