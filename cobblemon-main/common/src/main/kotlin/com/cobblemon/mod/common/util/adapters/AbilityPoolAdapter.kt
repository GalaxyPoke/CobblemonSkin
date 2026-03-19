/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.abilities.AbilityPool
import com.cobblemon.mod.common.api.abilities.PotentialAbility
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Adapter for [AbilityPool]s. This is responsible for deserializing the list and adding
 * them with the [PotentialAbility.priority]. The deserializing of each element of the list
 * is performed by the first non-null result of the [PotentialAbility.interpreters] list.
 *
 * @author Hiroku
 * @since July 28th, 2022
 */
object AbilityPoolAdapter : JsonDeserializer<AbilityPool> {
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): AbilityPool {
        val pool = AbilityPool()
        json.asJsonArray.forEach { element ->
            val potentialAbility = PotentialAbility.types.firstNotNullOfOrNull { it.parseFromJSON(element) }
                ?: throw IllegalStateException("Failed to interpret ability: $json")
            pool.add(potentialAbility.priority, potentialAbility)
        }
        return pool
    }
}