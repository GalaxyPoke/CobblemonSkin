/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.spawning.rules.selector.ConditionalSpawnablePositionSelector
import com.cobblemon.mod.common.api.spawning.rules.selector.ExpressionSpawnablePositionSelector
import com.cobblemon.mod.common.api.spawning.rules.selector.SpawnablePositionSelector
import com.cobblemon.mod.common.util.asExpressionLike
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

object SpawnablePositionSelectorAdapter : JsonDeserializer<SpawnablePositionSelector> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): SpawnablePositionSelector {
        return if (json.isJsonPrimitive || json.isJsonArray) {
            val expression = if (json.isJsonPrimitive) json.asString.asExpressionLike() else (json as JsonArray).asExpressionLike()
            ExpressionSpawnablePositionSelector().also { it.expression = expression }
        } else {
            json as JsonObject
            val type = json.get("type")?.asString ?: return ctx.deserialize(json, ConditionalSpawnablePositionSelector::class.java)
            val clazz = SpawnablePositionSelector.types[type] ?: throw IllegalArgumentException("Unknown spawn detail selector type: $type")
            ctx.deserialize(json, clazz)
        }
    }
}