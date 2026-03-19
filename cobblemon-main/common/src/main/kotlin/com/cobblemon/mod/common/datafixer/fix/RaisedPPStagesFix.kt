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

class RaisedPPStagesFix(output: Schema) : PokemonFix(output) {

    override fun fixPokemonData(dynamic: Dynamic<*>): Dynamic<*> {
        var result = processPPStages(dynamic, "MoveSet")
        result = processPPStages(result, "BenchedMoves")
        return result
    }

    private fun processPPStages(dynamic: Dynamic<*>, key: String): Dynamic<*> {
        var moveSetJson = dynamic.get(key).asListOpt { it }.result()
        if (!moveSetJson.isPresent) {
            return dynamic
        }

        var moves = moveSetJson.get()
        for (i in 0..moves.size) {
            var move = moves.getOrNull(i)
            if(move != null && move.get("RaisedPPStages").asNumber(0).toInt() > 3) {
                moves[i] = move.set("RaisedPPStages", dynamic.createInt(3))
            }
        }

        return dynamic.set(key, dynamic.createList(moves.stream()))
    }
}