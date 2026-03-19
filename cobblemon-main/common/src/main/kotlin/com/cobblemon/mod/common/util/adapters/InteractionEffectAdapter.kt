/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.api.interaction.DropItemEffect
import com.cobblemon.mod.common.api.interaction.GiveItemEffect
import com.cobblemon.mod.common.api.interaction.InteractionEffect
import com.cobblemon.mod.common.api.interaction.PlaySoundEffect
import com.cobblemon.mod.common.api.interaction.ScriptEffect
import com.cobblemon.mod.common.api.interaction.ShrinkItemEffect
import com.google.common.collect.HashBiMap
import com.google.gson.*
import java.lang.reflect.Type
import kotlin.reflect.KClass

object InteractionEffectAdapter : JsonDeserializer<InteractionEffect>, JsonSerializer<InteractionEffect> {
    private val types = HashBiMap.create<String, KClass<out InteractionEffect>>()

    init {
        this.registerType(DropItemEffect.ID, DropItemEffect::class)
        this.registerType(GiveItemEffect.ID, GiveItemEffect::class)
        this.registerType(PlaySoundEffect.ID, PlaySoundEffect::class)
        this.registerType(ShrinkItemEffect.ID, ShrinkItemEffect::class)
        this.registerType(ScriptEffect.ID, ScriptEffect::class)
    }

    fun <T : InteractionEffect> registerType(id: String, type: KClass<T>) {
        this.types[id.lowercase()] = type
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): InteractionEffect {
        val variant = json.asJsonObject.get("variant").asString.lowercase()
        val type = this.types[variant] ?: throw IllegalArgumentException("Cannot resolve evolution requirement type for variant $variant")
        return context.deserialize(json, type.java)
    }

    override fun serialize(src: InteractionEffect, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val json = context.serialize(src, src::class.java).asJsonObject
        val variant = this.types.inverse()[src::class] ?: throw IllegalArgumentException("Cannot resolve evolution requirement for type ${src::class.qualifiedName}")
        json.addProperty("variant", variant)
        return json
    }
}