/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon

import com.cobblemon.mod.common.pokemon.Pokemon

enum class PokemonSortMode(val comparator: Comparator<Pokemon?>, val reverseComparator: Comparator<Pokemon?>) {
    NAME({ it?.getDisplayName()?.string }),
    LEVEL({ it?.level }),
    TYPE({ it?.primaryType?.showdownId }),
    POKEDEX_NUMBER({ it?.species?.nationalPokedexNumber }),
    GENDER({ it?.gender });

    constructor(mapper: (Pokemon?) -> Comparable<*>?) : this(compareBy({ it == null }, mapper),
        compareBy<Pokemon?> { it == null }.thenComparing(compareBy(mapper).reversed()))

    fun comparator(descending: Boolean) = if (descending) reverseComparator else comparator
}