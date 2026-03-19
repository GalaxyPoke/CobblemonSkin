/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.components

import com.cobblemon.mod.common.api.cooking.Flavour
import com.cobblemon.mod.common.client.pot.CookingQuality
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

data class FlavourComponent(
    val flavours: Map<Flavour, Int>
) {
    companion object {
        val CODEC: Codec<FlavourComponent> = RecordCodecBuilder.create { builder ->
            builder.group(
                Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("flavours").forGetter { it.flavours.mapKeys { it.key.name } }
            ).apply(builder) { map -> FlavourComponent(map.mapKeys { Flavour.valueOf(it.key) }) }
        }

        val PACKET_CODEC: StreamCodec<ByteBuf, FlavourComponent> = ByteBufCodecs.fromCodec(CODEC)
    }

    fun getQuality(): CookingQuality {
        val sum = flavours.values.sum()
        return if (sum > 50) {
            CookingQuality.HIGH
        } else if (sum > 30) {
            CookingQuality.MEDIUM
        } else {
            CookingQuality.LOW
        }
    }

    fun getDominantFlavours(): List<Flavour> {
        val maxFlavorValue = flavours.values.maxOrNull()
        return flavours.filter { it.value == maxFlavorValue }.map { it.key }
    }
}