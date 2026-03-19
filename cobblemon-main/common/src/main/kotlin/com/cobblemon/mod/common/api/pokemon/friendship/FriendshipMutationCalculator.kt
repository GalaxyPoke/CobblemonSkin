/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.friendship

import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * A calculator for the amount of friendship to mutate based on different triggers.
 * You can find some default implementations in the [FriendshipMutationCalculator.Companion].
 *
 * @author Licious
 * @since October 29th, 2022
 */
fun interface FriendshipMutationCalculator {

    /**
     * Resolve the amount of friendship to mutate.
     *
     * @param pokemon The [Pokemon] being mutated.
     * @return The amount of friendship to mutate.
     */
    fun calculate(pokemon: Pokemon): Int

    companion object {

        /**
         * The calculator used for level up friendship yields in generation 8 mainline games.
         * For more information see this [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Friendship#Generation_VIII) entry.
         * It will yield 3 between 0 and 99 friendship, 2 between 100 and 199 otherwise 0.
         */
        val SWORD_AND_SHIELD_LEVEL_UP = FriendshipMutationCalculator { pokemon ->
            when {
                pokemon.friendship <= 99 -> 3
                pokemon.friendship <= 199 -> 2
                else -> 0
            }
        }

    }

}