/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.conditional

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Adapter for [RegistryLikeCondition]s. Its logic is that the implementation of this adapter must
 * provide a list of resolvers, and the deserializer will find the first resolver that returns a non-null
 * value for the [JsonElement].
 *
 * @author Hiroku
 * @since July 17th, 2022
 */
interface RegistryLikeAdapter<B> : JsonDeserializer<RegistryLikeCondition<B>> {
    val registryLikeConditions: MutableList<(JsonElement) -> RegistryLikeCondition<B>?>

    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext): RegistryLikeCondition<B> {
        return registryLikeConditions.firstNotNullOfOrNull { it(json) }
            ?: throw IllegalArgumentException("Unable to deserialize $json")
    }
}