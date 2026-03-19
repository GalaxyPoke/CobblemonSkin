/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.spawning.SpawnLoader.deserializingConditionClass
import com.cobblemon.mod.common.api.spawning.condition.AppendageCondition
import com.cobblemon.mod.common.api.spawning.condition.BasicSpawningCondition
import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Type adapter for deserializing a [SpawningCondition]. If there is some context for where this is being
 * deserialized, the type of spawnable position can be implied based on the default condition type registered
 * for the [SpawnDetail].
 *
 * If there isn't any context information, then there should be a "type" property to be used to look up the
 * condition class from [SpawningCondition.getByName] otherwise there will be a default to [BasicSpawningCondition].
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
object SpawningConditionAdapter : JsonDeserializer<SpawningCondition<*>> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): SpawningCondition<*> {
        val name = json.asJsonObject.get("type")?.asString

        val condition: SpawningCondition<*> = if (name == null) {
            if (deserializingConditionClass == null) {
                ctx.deserialize(json, BasicSpawningCondition::class.java)
            } else {
                ctx.deserialize(json, deserializingConditionClass)
            }
        } else {
            val clazz = SpawningCondition.getByName(name)
            if (clazz == null) {
                throw IllegalStateException("Unrecognized spawning condition type: $name")
            } else {
                ctx.deserialize(json, clazz)
            }
        }

        val appendageClasses = AppendageCondition.getAppendages(condition)
        appendageClasses.forEach {
            try {
                condition.appendages.add(ctx.deserialize(json, it))
            } catch (e: Exception) {
                LOGGER.error("Unable to deserialize appendage condition of type: ${it.simpleName}")
                throw e
            }
        }

        return condition
    }
}