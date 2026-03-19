/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.ai.config.BehaviourConfig
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * Adapter that deserializes a [BehaviourConfig] from a JSON object. If the JSON is just a string, it assumes
 * that that is the type and the rest of the properties should be default. This is just as shorthand.
 *
 * In general a behaviour config will be an object with a 'type' property matching something inside [BehaviourConfig.types].
 *
 * @see BehaviourConfig
 * @since October 13th, 2024
 * @author Hiroku
 */
object BehaviourConfigAdapter : JsonDeserializer<BehaviourConfig> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): BehaviourConfig? {
        if (json.isJsonPrimitive) {
            val type = json.asString.asIdentifierDefaultingNamespace()
            val clazz = BehaviourConfig.types[type]
                ?: throw IllegalArgumentException("Unknown behaviour config type: $type")
            return ctx.deserialize(JsonObject(), clazz)
        } else {
            val obj = json.asJsonObject
            val type = obj.get("type")?.asString?.asIdentifierDefaultingNamespace()
                ?: throw IllegalArgumentException("Missing behaviour config type. A behaviour config element must have a 'type' value.")
            val clazz = BehaviourConfig.types[type]
                ?: throw IllegalArgumentException("Unknown behaviour config type: $type")
            return ctx.deserialize(obj, clazz)
        }
    }
}