/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.getIntOrNull
import com.cobblemon.mod.common.util.getStringOrNull
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

class ObtainableItem(
    var item: ObtainableItemCondition? = null,
    var pickupPriority: Int = 0, // Will desire the highest value, a pokemon will immediately drop an item with negative priority
    var fullnessValue: Int = 0,
    var returnItem: ResourceLocation? = null,
    var onUseEffect: ExpressionLike? = null, // Molang Expression that plays against the entity when the item is consumed
) {
    val struct = ObjectValue(this).also {
        it.addFunction("item") { item?.let { identifier -> StringValue(identifier.toString()) } ?: DoubleValue.ZERO }
        it.addFunction("pickup_priority") { DoubleValue(pickupPriority) }
        it.addFunction("fullness_value") { DoubleValue(fullnessValue) }
        it.addFunction("return_item") { returnItem?.let { identifier -> StringValue(identifier.toString()) } ?: DoubleValue.ZERO }
        it.addFunction("on_use_effect") { onUseEffect?.let { expression -> StringValue(expression.getString()) } ?: DoubleValue.ZERO }

        it.addFunction("set_item") { params ->
            val identifier = params.getStringOrNull(0)?.asIdentifierDefaultingNamespace(namespace = "minecraft")
            item = identifier?.let(::IdentifierObtainableItemCondition)
        }
        it.addFunction("set_tag") { params ->
            val identifier = params.getStringOrNull(0)?.replace("#", "")?.asIdentifierDefaultingNamespace(namespace = "minecraft")
            item = identifier?.let { TagObtainableItemCondition(TagKey.create(Registries.ITEM, it)) }
        }
        it.addFunction("set_condition") { params ->
            val condition = params.getStringOrNull(0)?.asExpressionLike()
            item = condition?.let(::ExpressionObtainableItemCondition)
        }
        it.addFunction("set_pickup_priority") { params -> pickupPriority = params.getIntOrNull(0) ?: 0 }
        it.addFunction("set_fullness_value") { params -> fullnessValue = params.getIntOrNull(0) ?: 0 }
        it.addFunction("set_return_item") { params -> returnItem = params.getStringOrNull(0)?.asIdentifierDefaultingNamespace(namespace = "minecraft") }
        it.addFunction("set_on_use_effect") { params -> onUseEffect = params.getStringOrNull(0)?.asExpressionLike() }
    }
}