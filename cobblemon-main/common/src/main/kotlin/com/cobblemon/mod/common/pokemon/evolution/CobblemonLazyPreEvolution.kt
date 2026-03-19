/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.evolution.PreEvolution
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace

// We use this to "lazy" load a pre evolution since we can't validate all forms and species during species loading
internal class CobblemonLazyPreEvolution(private val rawData: String) : PreEvolution {

    private val properties: PokemonProperties
        get() = PokemonProperties.parse(this.rawData)

    private val lazySpecies: Species by lazy {
        this.properties.species?.asIdentifierDefaultingNamespace()?.let { PokemonSpecies.getByIdentifier(it) } ?: throw IllegalArgumentException("A PreEvolution needs a valid species")
    }

    private val lazyForm: FormData by lazy {
        this.properties.form?.let { formId -> this.species.forms.firstOrNull { it.formOnlyShowdownId().equals(formId, true) } } ?: this.species.standardForm
    }

    override val species: Species
        get() = this.lazySpecies
    override val form: FormData
        get() = this.lazyForm
}
