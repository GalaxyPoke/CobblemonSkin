/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.spawning.IntRanges
import com.cobblemon.mod.common.api.spawning.TimeRange
import com.cobblemon.mod.common.util.isInt
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 * Adapter for serializing and deserializing some kind of [IntRanges]. It deserializes from comma separated int ranges
 * in either name (referencing [ranges]) or in the format min-max
 *
 * For example, it can deserialize "day,18000-20000,100" as a [TimeRange] if constructed with [TimeRange.timeRanges].
 *
 * @author Hiroku
 * @since January 26th, 2022
 */
class IntRangesAdapter<T : IntRanges>(val ranges: Map<String, T>, val initializer: (Array<IntRange>) -> T) : JsonDeserializer<T>, JsonSerializer<T> {
    override fun serialize(src: T, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.ranges.joinToString { "${it.first}-${it.last}" })
    }

    override fun deserialize(json: JsonElement, t: Type, ctx: JsonDeserializationContext): T {
        val str = json.asString
        val splits = str.split(",")
        if (splits.isEmpty()) {
            return initializer(emptyArray())
        }

        val ranges = mutableListOf<IntRange>()
        splits.forEach {
            val range = it.split("-")
            if (range.size == 2 && range[0].isInt() && range[1].isInt()) {
                ranges.add(range[0].toInt()..range[1].toInt())
            } else if (range.size == 1) {
                val matchingRange = this.ranges[range[0].lowercase()]
                if (matchingRange != null) {
                    ranges.addAll(matchingRange.ranges)
                } else if (range[0].isInt()) {
                    ranges.add(range[0].toInt()..range[0].toInt())
                }
            }
        }
        return initializer(ranges.toTypedArray())
    }
}