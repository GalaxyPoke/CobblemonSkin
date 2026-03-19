/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.moves.animations.keyframes.ActionEffectKeyframe
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * Map adapter for [ActionEffectKeyframe]
 *
 * @author Hiroku
 * @since October 21st, 2023
 */
object ActionEffectKeyframeAdapter : JsonDeserializer<ActionEffectKeyframe> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): ActionEffectKeyframe {
        return if (json.isJsonPrimitive) {
            val clazz = ActionEffectKeyframe.types[json.asString]
                ?: throw IllegalArgumentException("Unrecognized action effect keyframe type: ${json.asJsonPrimitive}")
            clazz.getConstructor().newInstance()
        } else {
            json as JsonObject
            val typeString = json.get("type").asString
            val clazz = ActionEffectKeyframe.types[typeString]
                ?: throw IllegalArgumentException("Unrecognized action effect keyframe type: $typeString")
            ctx.deserialize(json, clazz)
        }
    }
}