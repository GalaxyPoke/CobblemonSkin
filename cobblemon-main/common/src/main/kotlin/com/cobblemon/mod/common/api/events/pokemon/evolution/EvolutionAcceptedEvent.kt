/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon.evolution

import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.moLangFunctionMap
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Fired when an evolution is accepted.
 * Canceling will not notify users nor remove the evolution from the pending list.
 *
 * @param pokemon The [Pokemon] about to evolve.
 * @param evolution The [Evolution] being used.
 *
 * @author Licious
 * @since April 28th, 2022
 */
data class EvolutionAcceptedEvent(
    override val pokemon: Pokemon,
    override val evolution: Evolution
) : Cancelable(), EvolutionEvent {
    val context = mutableMapOf<String, MoValue>(
        "pokemon" to pokemon.struct,
        "evolution" to evolution.asMoLangValue()
    )

    val functions = moLangFunctionMap(cancelFunc)
}