/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.types.adapters

import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

object ElementalTypeAdapter: JsonSerializer<ElementalType>, JsonDeserializer<ElementalType> {
    override fun serialize(src: ElementalType, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.showdownId)
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ElementalType {
        return ElementalTypes.getOrException(json.asString)
    }
}