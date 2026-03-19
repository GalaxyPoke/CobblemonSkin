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
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 * Adapts an IntRange into a simple hyphenated integer pair string. IntRange(2, 4) is serialized as
 * "2-4", and one-element ranges are serialized as single integers such that IntRange(10, 10)
 * serializes as "10".
 *
 * @author Hiroku, Qu
 * @since February 14th, 2022
 */
object IntRangeAdapter : JsonSerializer<IntRange>, JsonDeserializer<IntRange> {

    private val PATTERN = "(-?\\d+)-?(-?\\d+)?".toRegex()

    override fun serialize(range: IntRange, type: Type, ctx: JsonSerializationContext): JsonElement {
        return if (range.first == range.last) {
            JsonPrimitive(range.first)
        } else {
            JsonPrimitive("${range.first}-${range.last}")
        }
    }

    override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): IntRange {
        val (start, end) = PATTERN.find(json.asString)!!.destructured
        return if (end.isEmpty()) {
            IntRange(start.toInt(), start.toInt())
        } else {
            IntRange(start.toInt(), end.toInt())
        }
    }
}