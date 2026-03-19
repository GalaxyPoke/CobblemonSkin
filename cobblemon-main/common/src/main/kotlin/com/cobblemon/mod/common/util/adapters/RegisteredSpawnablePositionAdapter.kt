/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.spawning.position.SpawnablePositionType
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 * Adapter to a serialized [SpawnablePositionType] name to the actual registered object.
 *
 * @since January 28th, 2022
 * @author Hiroku
 */
object RegisteredSpawnablePositionAdapter : JsonSerializer<SpawnablePositionType<*>>, JsonDeserializer<SpawnablePositionType<*>> {
    override fun serialize(spawnablePosition: SpawnablePositionType<*>, type: Type, ctx: JsonSerializationContext) = JsonPrimitive(spawnablePosition.name)
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext) = SpawnablePosition.getByName(json.asString)
        ?: throw IllegalArgumentException("No such spawnable position: ${json.asString}")
}