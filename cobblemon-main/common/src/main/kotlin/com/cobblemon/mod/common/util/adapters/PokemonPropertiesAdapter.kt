/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

val pokemonPropertiesLongAdapter = PokemonPropertiesAdapter(true)
val pokemonPropertiesShortAdapter = PokemonPropertiesAdapter(false)

/**
 * Saves and loads a [PokemonProperties] instance with JSON. The [saveLong] construction parameter decides
 * whether the serialization process will write the properties as a full JSON object (true) or be lazy and
 * use the [PokemonProperties.originalString] value as a single string JSON field (false). The lazy way will
 * not work if the [PokemonProperties] wasn't created using [PokemonProperties.parse].
 *
 * @author Hiroku
 * @since February 13th, 2022
 */
open class PokemonPropertiesAdapter(val saveLong: Boolean) : JsonSerializer<PokemonProperties>, JsonDeserializer<PokemonProperties> {
    override fun serialize(props: PokemonProperties, type: Type, ctx: JsonSerializationContext): JsonElement {
        return if (saveLong) {
            props.saveToJSON()
        } else {
            JsonPrimitive(props.originalString)
        }
    }

    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): PokemonProperties {
        return if (json.isJsonPrimitive) {
            PokemonProperties.parse(json.asString)
        } else {
            PokemonProperties().loadFromJSON(json.asJsonObject)
        }
    }
}