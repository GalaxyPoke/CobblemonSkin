/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer.fix

import com.cobblemon.mod.common.util.DataKeys
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Dynamic

class ShoulderStateJsonFix(output: Schema) : PokemonFix(output) {
    override fun fixPokemonData(dynamic: Dynamic<*>): Dynamic<*> {
        val stateJson = dynamic.get("State").result()
        //State not here
        if (stateJson.isEmpty) {
            return dynamic
        }

        if (stateJson.get().mapValues.getOrThrow().isEmpty() ||
            !stateJson.get().mapValues.getOrThrow().containsKey(dynamic.createString(DataKeys.POKEMON_STATE_TYPE))) {
            return dynamic.remove("State")
        }

        return dynamic
    }
}