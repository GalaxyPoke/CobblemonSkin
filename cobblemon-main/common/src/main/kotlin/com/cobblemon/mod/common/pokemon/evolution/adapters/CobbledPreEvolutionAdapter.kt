/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.adapters

import com.cobblemon.mod.common.api.pokemon.evolution.PreEvolution
import com.cobblemon.mod.common.pokemon.evolution.CobblemonLazyPreEvolution
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

object CobblemonPreEvolutionAdapter : JsonDeserializer<PreEvolution>, JsonSerializer<PreEvolution> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PreEvolution {
        return CobblemonLazyPreEvolution(json.asString)
    }

    override fun serialize(src: PreEvolution, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        if (src.form == src.species.standardForm) {
            return JsonPrimitive(src.species.resourceIdentifier.toString())
        }
        return JsonPrimitive("${src.species.resourceIdentifier} form=${src.form.name}")
    }

}