/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import org.joml.Vector3f

object Vector3fAdapter : JsonDeserializer<Vector3f>, JsonSerializer<Vector3f> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): Vector3f {
        json as JsonArray
        return Vector3f(json[0].asFloat, json[1].asFloat, json[2].asFloat)
    }

    override fun serialize(src: Vector3f, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonArray()
            .also {
                it.add(src.x)
                it.add(src.y)
                it.add(src.z)
            }
    }
}