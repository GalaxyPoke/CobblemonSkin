/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer.fix

import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Dynamic
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.resources.RegistryOps

class NicknameFix(outputSchema: Schema) : PokemonFix(outputSchema) {
    override fun fixPokemonData(dynamic: Dynamic<*>): Dynamic<*> {
        val nickname = dynamic.get("Nickname").asString().result()
        if (!nickname.isPresent) {
            return dynamic // no nickname
        }
        var jsonElement: JsonElement
        try {
            jsonElement = JsonParser.parseString(nickname.get())
        } catch (_: JsonParseException) {
            return dynamic // no json
        }
        ComponentSerialization.CODEC.parse(asJsonOps(dynamic.ops), jsonElement).result().ifPresent {
            if (dynamic.value is CompoundTag) {
                var rootTag = dynamic.value as CompoundTag
                rootTag.put("Nickname", ComponentSerialization.CODEC.encodeStart(dynamic.ops as RegistryOps<CompoundTag>, it).result().get())
            }
            else if (dynamic.value is JsonElement) {
                var rootTag = (dynamic.value as JsonElement).asJsonObject
                rootTag.remove("Nickname")
                rootTag.add("Nickname",  ComponentSerialization.CODEC.encodeStart(asJsonOps(dynamic.ops), it).result().get())
            }
        }

        return dynamic
    }

    private fun <T> asJsonOps(ops: DynamicOps<T>): DynamicOps<JsonElement> {
        return if (ops is RegistryOps<T>) {
            ops.withParent(JsonOps.INSTANCE)
        } else {
            JsonOps.INSTANCE
        }
    }
}