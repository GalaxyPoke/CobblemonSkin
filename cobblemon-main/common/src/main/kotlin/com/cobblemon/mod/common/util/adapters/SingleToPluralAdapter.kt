/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Generic adapter for converting a field that is intended to be an iterable but we support entering single
 * values in the JSON for succinctness. You have to provide the class of the elements and a way of converting
 * a list of elements into the collection type you want.
 *
 * @author Hiroku
 * @since October 29th, 2023
 */
class SingleToPluralAdapter<T, C : Iterable<T>>(val clazz: Class<T>, val converter: (List<T>) -> C) : JsonDeserializer<C> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): C {
        return (if (json.isJsonArray) {
            json.asJsonArray.map<JsonElement, T> { ctx.deserialize(it, clazz) }
        } else {
            listOf(ctx.deserialize(json, clazz))
        }).let(converter)
    }
}