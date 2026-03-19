/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.util.asTranslated
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import java.lang.reflect.Type
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization

/**
 * Kinda like [TextAdapter] but it treats them as translatables.
 *
 * @author Hiroku
 * @since November 26th, 2024
 */
object TranslatedTextAdapter : JsonDeserializer<Component> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext) : Component {
        return if (json.isJsonObject) {
            ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, json).result().get().first
        } else {
            json.asString.asTranslated()
        }
    }
}