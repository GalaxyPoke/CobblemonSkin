/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.crafting

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.util.ByIdMap
import net.minecraft.util.StringRepresentable
import java.util.function.IntFunction

enum class CookingPotBookCategory(
    private val categoryName: String,
    private val categoryId: Int
) : StringRepresentable {
    FOODS("foods", 0),
    MEDICINES("medicines", 1),
    COMPLEX_DISHES("complex_dishes", 2),
    MISC("misc", 3);

    override fun getSerializedName() = categoryName
    private fun getCategoryId() = categoryId

    companion object {
        val CODEC: Codec<CookingPotBookCategory> = Codec.STRING.flatXmap(
            { name ->
                val category = CookingPotBookCategory.entries.firstOrNull { it.categoryName.equals(name, ignoreCase = true) }
                if (category != null) {
                    DataResult.success(category)
                } else {
                    DataResult.error { "Unknown category: $name" }
                }
            }, { category ->
                DataResult.success(category.categoryName)
            }
        )
        val BY_ID: IntFunction<CookingPotBookCategory> = ByIdMap.continuous(
            CookingPotBookCategory::getCategoryId,
            CookingPotBookCategory.entries.toTypedArray(),
            ByIdMap.OutOfBoundsStrategy.ZERO
        )
        val STREAM_CODEC: StreamCodec<ByteBuf, CookingPotBookCategory> = ByteBufCodecs.idMapper(BY_ID, CookingPotBookCategory::getCategoryId)
    }
}