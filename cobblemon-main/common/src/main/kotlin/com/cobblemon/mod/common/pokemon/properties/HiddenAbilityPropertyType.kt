/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.properties

import com.cobblemon.mod.common.api.abilities.CommonAbilityType
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType

/**
 * A type of [CustomPokemonPropertyType] which asserts that the Pokémon's ability
 * must be a hidden ability.
 *
 * @author Hiroku
 * @since November 1st, 2022
 */
object HiddenAbilityPropertyType : CustomPokemonPropertyType<HiddenAbilityProperty> {
    override val keys = setOf("hiddenability", "ha")
    override val needsKey = true
    override fun fromString(value: String?) =
        when {
            value == null || value.lowercase() in listOf("true", "yes") -> HiddenAbilityProperty(true)
            value.lowercase() in listOf("false", "no") -> HiddenAbilityProperty(false)
            else -> null
        }
    override fun examples() = setOf("yes", "no")
}

class HiddenAbilityProperty (var value: Boolean) : CustomPokemonProperty {
    override fun asString() = "hiddenability"
    override fun apply(pokemon: Pokemon) {
        val possible = pokemon.form.abilities.mapping.flatMap { it.value }
            .filter { it.type == getAbilityType() }
        val picked = possible.randomOrNull() ?: return
        pokemon.updateAbility(picked.template.create(false, picked.priority))
    }

    override fun matches(pokemon: Pokemon) = pokemon.form.abilities.mapping
        .flatMap { it.value }
        .find { it.template == pokemon.ability.template }
        ?.type == getAbilityType()

    private fun getAbilityType() = if (this.value) HiddenAbilityType else CommonAbilityType
}