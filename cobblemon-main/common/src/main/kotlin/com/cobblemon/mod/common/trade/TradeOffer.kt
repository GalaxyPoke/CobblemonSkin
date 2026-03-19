/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.trade

import com.cobblemon.mod.common.pokemon.Pokemon

class TradeOffer {
    var pokemon: Pokemon? = null
    var accepted = false

    fun updateOffer(pokemon: Pokemon?) {
        this.pokemon = pokemon
        accepted = false
    }
}