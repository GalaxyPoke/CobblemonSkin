/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.cooking

import com.cobblemon.mod.common.api.conditional.ITEM_REGISTRY_LIKE_CODEC
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.conditional.RegistryLikeIdentifierCondition
import com.cobblemon.mod.common.api.fishing.SpawnBait
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.annotations.SerializedName
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Item
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

data class Seasoning(
    val ingredient: RegistryLikeCondition<Item>,
    @SerializedName("flavours", alternate = ["flavors"])
    val flavours: Map<Flavour, Int>? = null,
    @SerializedName("colour", alternate = ["color"])
    val colour: DyeColor,
    @SerializedName("baitEffects")
    val baitEffects: List<SpawnBait.Effect>? = null,
    @SerializedName("food")
    val food: Food? = null,
    @SerializedName("mobEffects")
    val mobEffects: List<SerializableMobEffectInstance>? = null
) {
    companion object {
        val CODEC: Codec<Seasoning> = RecordCodecBuilder.create { builder ->
            builder.group(
                ITEM_REGISTRY_LIKE_CODEC.fieldOf("ingredient").forGetter { it.ingredient },
                Codec.unboundedMap(Flavour.CODEC, Codec.INT).optionalFieldOf("flavours").forGetter { Optional.ofNullable(it.flavours) },
                DyeColor.CODEC.fieldOf("colour").forGetter { it.colour },
                SpawnBait.Effect.CODEC.listOf().optionalFieldOf("baitEffects").forGetter { Optional.ofNullable(it.baitEffects) },
                Food.CODEC.optionalFieldOf("food").forGetter { Optional.ofNullable(it.food) },
                SerializableMobEffectInstance.CODEC.listOf().optionalFieldOf("mobEffects").forGetter { Optional.ofNullable(it.mobEffects) }
            ).apply(builder) { item, flavours, colour, baitEffects, food, mobEffects ->
                Seasoning(
                    item,
                    flavours.getOrNull(),
                    colour,
                    baitEffects.getOrNull(),
                    food.getOrNull(),
                    mobEffects.getOrNull()
                )
            }
        }

        val STREAM_CODEC: StreamCodec<ByteBuf, Seasoning> = ByteBufCodecs.fromCodec(CODEC)
    }
}
