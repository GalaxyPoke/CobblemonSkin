/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.pokemon.moves.Learnset
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

object LearnsetAdapter : JsonDeserializer<Learnset> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): Learnset {
        val array = json.asJsonArray
        val learnset = Learnset()
        for (element in array) {
            var added = false
            interpreterLoop@
            for (interpreter in Learnset.interpreters) {
                if (interpreter.loadMove(element, learnset)) {
                    added = true
                    break@interpreterLoop
                }
            }

            if (!added) {
                LOGGER.error("Unable to load entry from learnset: $element")
            }
        }
        return learnset
    }
}