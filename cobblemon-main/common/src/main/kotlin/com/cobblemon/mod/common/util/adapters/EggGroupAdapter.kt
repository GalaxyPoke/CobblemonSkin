/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

object EggGroupAdapter : JsonDeserializer<EggGroup>, JsonSerializer<EggGroup> {

    // Safe to just cache
    private val eggGroups = EggGroup.values()

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): EggGroup {
        val rawID = json.asString
        return this.eggGroups.firstOrNull { eggGroup -> eggGroup.name.equals(rawID, true) }
            ?: throw IllegalStateException("Failed to resolve egg group from: $rawID")
    }

    override fun serialize(src: EggGroup, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        // We prettify the enum value instead of PokeAPI format due to it being the "correct" english name
        return JsonPrimitive(
            src.name.lowercase()
                .split("_")
                .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
        )
    }

}