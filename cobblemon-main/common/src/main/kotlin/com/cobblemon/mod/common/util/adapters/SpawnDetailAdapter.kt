/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.spawning.SpawnDetailPresets
import com.cobblemon.mod.common.api.spawning.SpawnLoader
import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.preset.SpawnDetailPreset
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.singularToPluralList
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * A simple map adapter for [SpawnDetail] implementations. This adapter does some heavy logic to apply
 * [SpawnDetailPreset]s and to carefully derive the default subclass to be used when deserializing
 * [SpawningCondition]s.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
object SpawnDetailAdapter : JsonDeserializer<SpawnDetail> {
    override fun deserialize(element: JsonElement, type: Type, ctx: JsonDeserializationContext): SpawnDetail {
        element as JsonObject
        element.singularToPluralList("preset")
        val presetNames = element.get("presets")?.asJsonArray?.map { it.asString }?.toMutableSet() ?: mutableSetOf()
        val presets = presetNames.mapNotNull {
            val preset = SpawnDetailPresets.presets[it.asIdentifierDefaultingNamespace()]
            if (preset == null) {
                LOGGER.error("Unknown preset name: $it.")
            }
            return@mapNotNull preset
        }
        val firstType = presets.firstNotNullOfOrNull { it.spawnDetailType }


        // Move `condition` into the `conditions`
        element.singularToPluralList("condition")

        // Move `anticondition` into the `anticonditions`
        element.singularToPluralList("anticondition")

        // Move `weightMultiplier` into `weightMultipliers`
        element.singularToPluralList("weightMultiplier")

        if (element.has("weightMultipliers")) {
            element.get("weightMultipliers").asJsonArray.forEach { json ->
                json as JsonObject
                json.singularToPluralList("condition")
                json.singularToPluralList("anticondition")
            }
        }

        val spawnDetailTypeName = firstType
            ?: element.get("type")?.asString
            ?: throw IllegalStateException("Spawn detail type name not mentioned in either presets or in spawn detail.")
        val registeredSpawnDetail = SpawnDetail.spawnDetailTypes[spawnDetailTypeName]
            ?: throw IllegalStateException("Unrecognized spawn detail type name: $spawnDetailTypeName.")
        val ctxName = presets.firstNotNullOfOrNull { it.spawnablePositionType?.name }
            ?: (element.get("spawnablePositionType") ?: element.get("context")).asString
        val ctxType = SpawnablePosition.getByName(ctxName)
            ?: throw IllegalStateException("Unrecognized context name: $ctxName")
        SpawnLoader.deserializingConditionClass = SpawningCondition.getByName(ctxType.defaultCondition)
            ?: throw IllegalStateException("There is no spawning condition registered with the name '${ctxType.defaultCondition}'")
        val detail = registeredSpawnDetail.deserializeDetail(element, ctx)
        presets.forEach { it.apply(detail) }
        if (detail.bucket.name.isBlank()) {
            throw IllegalStateException("No bucket was specified for spawn: ${detail.id}")
        }
        detail.autoLabel()
        return detail
    }
}