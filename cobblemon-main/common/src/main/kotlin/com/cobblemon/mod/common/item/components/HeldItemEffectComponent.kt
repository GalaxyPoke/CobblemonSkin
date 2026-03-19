/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.components

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation

data class HeldItemEffectComponent(val showdownId: String, val consumed: Boolean = false) {
    companion object {
        val CODEC: Codec<HeldItemEffectComponent> = RecordCodecBuilder.create { builder ->
            builder.group(
                Codec.STRING.fieldOf("showdownId").forGetter { it.showdownId },
                Codec.BOOL.fieldOf("consumed").forGetter { it.consumed },
            ).apply(builder, ::HeldItemEffectComponent)
        }

        val PACKET_CODEC: StreamCodec<ByteBuf, HeldItemEffectComponent> = ByteBufCodecs.fromCodec(CODEC)
    }

    override fun toString(): String { return showdownId }

    override fun equals(other: Any?): Boolean =
        other is HeldItemEffectComponent
            && this.showdownId == other.showdownId
                && this.consumed == other.consumed

    override fun hashCode(): Int {
        var result = consumed.hashCode()
        result = 31 * result + showdownId.hashCode()
        return result
    }
}
