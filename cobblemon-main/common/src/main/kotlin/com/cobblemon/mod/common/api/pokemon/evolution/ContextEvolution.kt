/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.evolution

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.evolution.variants.ItemInteractionEvolution
import com.cobblemon.mod.common.pokemon.evolution.variants.TradeEvolution

/**
 * Represents an evolution of a [Pokemon] that can only occur during specific actions and with added context.
 * For the default implementations see [ItemInteractionEvolution] & [TradeEvolution].
 *
 * @param RC The context given at runtime when querying the [Evolution].
 * @param TC The context that is serialized from JSON during species loading, this is what the [RC] is expected to match against.
 * @author Licious
 * @since March 19th, 2022
 */
interface ContextEvolution<RC, TC> : Evolution {

    /**
     * The target context for this [Evolution] to even be tested.
     */
    val requiredContext: TC

    /**
     * Attempts to evolve the given [Pokemon] under the given context of type [RC].
     *
     * @param pokemon The [Pokemon] attempting to evolve.
     * @param context The context of this query.
     * @return If the evolution was successful.
     */
    fun attemptEvolution(pokemon: Pokemon, context: RC): Boolean {
        if (this.testContext(pokemon, context) && super.test(pokemon)) {
            return super.evolve(pokemon)
        }
        return false
    }

    /**
     * Checks if the given context is valid for the [requiredContext].
     *
     * @param pokemon The [Pokemon] attempting to evolve.
     * @param context The context of this query.
     * @return If the context matched the [requiredContext].
     */
    fun testContext(pokemon: Pokemon, context: RC): Boolean

}