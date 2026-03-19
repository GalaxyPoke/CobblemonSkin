/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.withQueryValue
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

/**
 * Abstraction for the conditions under which an [ObtainableItem] is satisfied
 * by a given [ItemStack].
 *
 * @author Hiroku
 * @since August 10th, 2025
 */
interface ObtainableItemCondition {
    companion object {
        fun parseFromString(str: String): ObtainableItemCondition {
            if (str.isBlank()) {
                return ExpressionObtainableItemCondition("true".asExpressionLike())
            } else if (str.startsWith("#")) {
                val tagId = str.removePrefix("#").asIdentifierDefaultingNamespace(namespace = "minecraft")
                return TagObtainableItemCondition(TagKey.create(Registries.ITEM, tagId))
            } else if (":" in str) {
                val itemId = ResourceLocation.parse(str)
                return IdentifierObtainableItemCondition(itemId)
            } else {
                return ExpressionObtainableItemCondition(str.asExpressionLike())
            }
        }
    }

    override fun toString(): String
    fun isItemObtainable(registryAccess: RegistryAccess, itemStack: ItemStack): Boolean
}

class IdentifierObtainableItemCondition(private val itemId: ResourceLocation): ObtainableItemCondition {
    override fun toString(): String = itemId.toString()
    override fun isItemObtainable(registryAccess: RegistryAccess, itemStack: ItemStack): Boolean {
        return itemStack.itemHolder.`is`(itemId)
    }
}

class TagObtainableItemCondition(private val tagKey: TagKey<Item>): ObtainableItemCondition {
    override fun toString(): String = "#${tagKey.location}"
    override fun isItemObtainable(registryAccess: RegistryAccess, itemStack: ItemStack): Boolean {
        return itemStack.itemHolder.`is`(tagKey)
    }
}

class ExpressionObtainableItemCondition(private val expression: ExpressionLike): ObtainableItemCondition {
    companion object {
        val runtime = MoLangRuntime().setup()
    }

    override fun toString() = expression.getString()
    override fun isItemObtainable(registryAccess: RegistryAccess, itemStack: ItemStack): Boolean {
        runtime.withQueryValue("item", itemStack.asMoLangValue(registryAccess))
        return runtime.resolveBoolean(expression)
    }
}

object ObtainableItemConditionAdapter : JsonDeserializer<ObtainableItemCondition> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        ctx: JsonDeserializationContext
    ): ObtainableItemCondition {
        if (json.isJsonArray) {
            return ExpressionObtainableItemCondition(json.asJsonArray.map { it.asString }.asExpressionLike())
        } else {
            val str = json.asString
            return ObtainableItemCondition.parseFromString(str)
        }
    }
}