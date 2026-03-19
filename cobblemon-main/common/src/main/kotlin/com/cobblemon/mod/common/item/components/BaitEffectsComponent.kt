/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.components

import com.cobblemon.mod.common.api.fishing.SpawnBait
import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation

/**
 * This component contains a list of [SpawnBait.Effect] [ResourceLocation]s that should be applied for a
 * cake or when this item is on a fishing rod.
 *
 * @author Hiroku
 * @since March 18th, 2025
 */
class BaitEffectsComponent(
    val effects: List<ResourceLocation>
) {
    companion object {
        val CODEC: Codec<BaitEffectsComponent> = ResourceLocation.CODEC.listOf().xmap(::BaitEffectsComponent, BaitEffectsComponent::effects)
        val PACKET_CODEC: StreamCodec<ByteBuf, BaitEffectsComponent> = ByteBufCodecs.fromCodec(CODEC)
    }

    override fun equals(other: Any?): Boolean {
        return other is BaitEffectsComponent && effects == other.effects
    }

    override fun hashCode(): Int {
        return effects.hashCode()
    }
}