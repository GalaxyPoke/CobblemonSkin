/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.bedrockk.molang.runtime.value.MoValue
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

object MoValueAdapter : JsonSerializer<MoValue>, JsonDeserializer<MoValue> {
    override fun serialize(src: MoValue, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return MoValue.writeToJson(src) ?: JsonObject()
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): MoValue {
        return MoValue.of(json)
    }
}