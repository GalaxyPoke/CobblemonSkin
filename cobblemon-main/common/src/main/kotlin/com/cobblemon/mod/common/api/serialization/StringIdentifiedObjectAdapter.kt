/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 * Simple adapter which expects a String and will turn into an object based on some resolver.
 *
 * @author Hiroku
 * @since July 18th, 2022
 */
class StringIdentifiedObjectAdapter<T>(
    val fromString: (String) -> T,
    val toString: ((T) -> String)? = null,
) : JsonDeserializer<T>, JsonSerializer<T> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext) = fromString(json.asString)
    override fun serialize(src: T, type: Type?, ctx: JsonSerializationContext?): JsonElement? {
        val stringValue = toString?.invoke(src) ?: throw IllegalStateException("No toString function provided for serialization.")
        return JsonPrimitive(stringValue)
    }
}