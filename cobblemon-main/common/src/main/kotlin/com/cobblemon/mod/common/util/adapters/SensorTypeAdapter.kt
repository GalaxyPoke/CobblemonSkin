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
import java.lang.reflect.Type
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.sensing.SensorType

/**
 * Deserializes a [SensorType] from an identifier string field like cobblemon:battling_pokemon.
 *
 * @author Hiroku
 * @since June 24th, 2025
 */
object SensorTypeAdapter : JsonDeserializer<SensorType<*>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SensorType<*>? {
        val identifier = ResourceLocation.tryParse(json.asString)
            ?: throw IllegalArgumentException("Invalid identifier: ${json.asString}")
        return BuiltInRegistries.SENSOR_TYPE.get(identifier)
    }
}