/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.abilities.AbilityTemplate
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

object AbilityTemplateAdapter: JsonSerializer<AbilityTemplate>, JsonDeserializer<AbilityTemplate> {
    override fun serialize(src: AbilityTemplate, type: Type, ctx: JsonSerializationContext) = JsonPrimitive(src.name)
    override fun deserialize(json: JsonElement, type: Type, ctx: JsonDeserializationContext) = Abilities.getOrException(json.asString)
}