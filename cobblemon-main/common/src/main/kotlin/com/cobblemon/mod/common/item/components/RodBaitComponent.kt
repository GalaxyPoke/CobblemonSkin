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
import net.minecraft.world.item.ItemStack

/**
 * A simple component that contains a reference to the [com.cobblemon.mod.common.api.fishing.SpawnBait].
 *
 * @author Hiroku
 * @since June 9th, 2024
 */
class RodBaitComponent(val stack: ItemStack = ItemStack.EMPTY) {
    companion object {
        val CODEC: Codec<RodBaitComponent> = RecordCodecBuilder.create { builder -> builder.group(
            ItemStack.CODEC.optionalFieldOf("stack", ItemStack.EMPTY).forGetter { it.stack }
        ).apply(builder) { stack -> RodBaitComponent(stack) } }

        val PACKET_CODEC: StreamCodec<ByteBuf, RodBaitComponent> = ByteBufCodecs.fromCodec(CODEC)
    }

    override fun hashCode() = ItemStack.hashItemAndComponents(stack)
    override fun equals(other: Any?) = other === this || (other is RodBaitComponent && ItemStack.matches(other.stack, stack))
}