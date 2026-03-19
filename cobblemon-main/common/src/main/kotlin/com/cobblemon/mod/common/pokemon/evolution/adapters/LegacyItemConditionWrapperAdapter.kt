/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.evolution.adapters

import com.cobblemon.mod.common.util.adapters.CodecBackedAdapter
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.mojang.serialization.JsonOps
import net.minecraft.advancements.critereon.ItemCustomDataPredicate
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.advancements.critereon.ItemSubPredicates
import net.minecraft.advancements.critereon.NbtPredicate
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import java.lang.reflect.Type

// This is an ugly adapter because breaking changes are scary and people would ;(
object LegacyItemConditionWrapperAdapter : JsonDeserializer<ItemPredicate>, JsonSerializer<ItemPredicate> {

    private const val TAG_PREFIX = "#"
    private const val LEGACY_ITEM = "item"
    private const val LEGACY_NBT = "nbt"

    private val codecAdapter = CodecBackedAdapter(ItemPredicate.CODEC)

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ItemPredicate {
        if (json.isJsonPrimitive) {
            return this.createBuilderForItem(json.asString).build()
        }
        else if (json.isJsonObject) {
            val jObject = json.asJsonObject
            if (jObject.size() == 2 && jObject.has(LEGACY_ITEM) && jObject.has(LEGACY_NBT)) {
                val builder = this.createBuilderForItem(jObject.get(LEGACY_ITEM).asString)
                val nbtPredicate = NbtPredicate.CODEC.decode(JsonOps.INSTANCE, jObject.get(LEGACY_NBT)).result().get().first
                val itemCustomDataPredicate = ItemCustomDataPredicate(nbtPredicate)
                return builder
                    .withSubPredicate(ItemSubPredicates.CUSTOM_DATA, itemCustomDataPredicate)
                    .build()
            }
        }
        return this.codecAdapter.deserialize(json, typeOfT, context)
    }

    override fun serialize(src: ItemPredicate, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return this.codecAdapter.serialize(src, typeOfSrc, context)
    }

    private fun createBuilderForItem(raw: String): ItemPredicate.Builder {
        val isTag = raw.startsWith(TAG_PREFIX)
        val builder = ItemPredicate.Builder.item()
        return ResourceLocation.read(if (isTag) raw.substring(1) else raw)
            .mapOrElse(
                { id ->
                    if (isTag) {
                        val tag = TagKey.create(Registries.ITEM, id)
                        builder.of(tag)
                    } else {
                        BuiltInRegistries.ITEM.getOptional(id)
                            .map { builder.of(it) }
                            .orElseGet { builder }
                    }
                },
                { builder }
            )
    }

}