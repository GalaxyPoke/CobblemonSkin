/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.google.gson.*
import java.lang.reflect.Type

/**
 * A verbose [IntRange] adapter used to be more consistent with Minecraft when used alongside their conditional ranges in datapack data.
 *
 * @author Licious
 * @since December 2nd, 2022
 */
object VerboseIntRangeAdapter : JsonDeserializer<IntRange>, JsonSerializer<IntRange> {

    private const val MIN = "min"
    private const val MAX = "max"

    override fun deserialize(jElement: JsonElement, type: Type, context: JsonDeserializationContext): IntRange {
        val json = jElement.asJsonObject
        val min = json.get(MIN).asInt
        val max = json.get(MAX).asInt
        return IntRange(min, max)
    }

    override fun serialize(range: IntRange, type: Type, context: JsonSerializationContext) = JsonObject().apply {
        addProperty(MIN, range.first)
        addProperty(MAX, range.last)
    }
}