/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.net.serializers

import com.cobblemon.mod.common.api.riding.stats.RidingStat
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.syncher.EntityDataSerializer

object RideBoostsDataSerializer : EntityDataSerializer<Map<RidingStat, Float>> {
    val ID = cobblemonResource("ride_boosts")
    fun read(buf: RegistryFriendlyByteBuf): Map<RidingStat, Float> {
        return buf.readMap(
            { RidingStat.valueOf(it.readString()) },
            { it.readFloat() }
        )
    }
    override fun copy(value: Map<RidingStat, Float>) = value.toMap()
    fun write(buf: RegistryFriendlyByteBuf, value: Map<RidingStat, Float>) {
        buf.writeMap(
            value,
            { _, it -> buf.writeString(it.name) },
            { _, it -> buf.writeFloat(it) }
        )
    }

    override fun codec() = StreamCodec.of(::write, ::read)
}