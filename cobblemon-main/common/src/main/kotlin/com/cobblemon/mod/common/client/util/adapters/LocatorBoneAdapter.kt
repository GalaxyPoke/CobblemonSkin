/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.util.adapters

import com.cobblemon.mod.common.client.render.models.blockbench.LocatorBone
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * This adapter allows [LocatorBone]s to be read. They sometimes present as just the offset array, but otherwise
 * are an object with offset and rotation clearly defined.
 *
 * @author Hiroku
 * @since February 10th, 2023
 */
object LocatorBoneAdapter : JsonDeserializer<LocatorBone> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): LocatorBone {
        val offset: List<Float>
        val rotation: List<Float>
        if (json is JsonArray) {
            offset = listOf(json[0].asFloat, json[1].asFloat, json[2].asFloat)
            rotation = listOf(0F, 0F, 0F)
        } else {
            json as JsonObject
            offset = json.get("offset")?.asJsonArray?.map { it.asFloat } ?: listOf(0F, 0F, 0F)
            rotation = json.get("rotation")?.asJsonArray?.map { it.asFloat } ?: listOf(0F, 0F, 0F)
        }
        return LocatorBone(
            offset = offset,
            rotation = rotation
        )
    }
}