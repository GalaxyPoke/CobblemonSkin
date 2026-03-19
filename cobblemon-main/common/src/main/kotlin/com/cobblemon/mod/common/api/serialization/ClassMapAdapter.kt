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
import java.lang.reflect.Type

/**
 * A simplistic JSON deserializer that relies on a mapping from a key to a class map to figure out how to deserialize
 * it.
 *
 * @author Hiroku
 * @since January 10th, 2022
 */
class ClassMapAdapter<T, K>(
    val mapping: MutableMap<K, Class<out T>>,
    val keyFromElement: (JsonElement) -> K
) : JsonDeserializer<T> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): T {
        val key = keyFromElement(json)
        val clazz = mapping[key] ?: throw IllegalStateException("Could not find class registered for key: $key")
        return ctx.deserialize(json, clazz)
    }
}