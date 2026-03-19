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
import net.minecraft.world.entity.ai.memory.MemoryModuleType

/**
 * Deserializes a [MemoryModuleType] from an identifier string field like cobblemon:pokemon_battle.
 *
 * @author Hiroku
 * @since June 24th, 2025
 */
object MemoryModuleTypeAdapter : JsonDeserializer<MemoryModuleType<*>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): MemoryModuleType<*>? {
        val identifier = ResourceLocation.tryParse(json.asString)
            ?: throw IllegalArgumentException("Invalid identifier: ${json.asString}")
        return BuiltInRegistries.MEMORY_MODULE_TYPE.get(identifier)
    }
}