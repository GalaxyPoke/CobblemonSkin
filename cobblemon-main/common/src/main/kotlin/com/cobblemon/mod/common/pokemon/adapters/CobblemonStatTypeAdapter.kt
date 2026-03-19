/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.adapters

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.StatTypeAdapter
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import java.lang.reflect.Type

/** Handles JSON adapting between a Stat and its serialized form; its id.*/
object CobblemonStatTypeAdapter : StatTypeAdapter {
    override fun deserialize(element: JsonElement, type: Type, context: JsonDeserializationContext): Stat {
        val identifier = element.asString.asIdentifierDefaultingNamespace()
        return Cobblemon.statProvider.fromIdentifierOrThrow(identifier)
    }

    override fun serialize(stat: Stat, type: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(stat.identifier.toString())
    }
}