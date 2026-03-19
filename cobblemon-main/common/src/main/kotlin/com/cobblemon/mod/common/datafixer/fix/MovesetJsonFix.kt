/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer.fix

import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Dynamic

class MovesetJsonFix(output: Schema) : PokemonFix(output) {
    override fun fixPokemonData(dynamic: Dynamic<*>): Dynamic<*> {
        val moveSetJson = dynamic.get("MoveSet").asMapOpt().result()
        //Moveset is probably already a list,
        if (!moveSetJson.isPresent) {
            return dynamic
        }

        return dynamic.set("MoveSet", dynamic.createList(moveSetJson.get().map { it.second }))
    }
}