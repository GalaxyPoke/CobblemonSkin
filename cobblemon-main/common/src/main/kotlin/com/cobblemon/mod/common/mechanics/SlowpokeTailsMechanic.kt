/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mechanics

import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Settings for the mechanic that regrows Slowpoke tails after they're sheared off.
 *
 * @author Hiroku
 * @since November 14th, 2025
 */
class SlowpokeTailsMechanic {
    var canShearSlowpoke = true
    var onlyRegrowWhenSentOut = false
    var regrowthSeconds = 1200
    var aspectThresholds = mapOf<Int, String>()

    fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(canShearSlowpoke)
        buffer.writeBoolean(onlyRegrowWhenSentOut)
        buffer.writeVarInt(regrowthSeconds)
        buffer.writeMap(
            aspectThresholds,
            { _, key -> buffer.writeVarInt(key) },
            { _, value -> buffer.writeUtf(value) }
        )
    }

    fun getAspects(regrowthSeconds: Int): Set<String> {
        return aspectThresholds
            .filter { regrowthSeconds < it.key }
            .map { it.value }
            .toSet()
    }

    companion object {

        fun decode(buffer: RegistryFriendlyByteBuf): SlowpokeTailsMechanic {
            val canShearSlowpoke = buffer.readBoolean()
            val onlyRegrow = buffer.readBoolean()
            val seconds = buffer.readVarInt()
            val thresholds = buffer.readMap(
                { buffer.readVarInt() },
                { buffer.readUtf() }
            )

            return SlowpokeTailsMechanic().apply {
                this.canShearSlowpoke = canShearSlowpoke
                onlyRegrowWhenSentOut = onlyRegrow
                regrowthSeconds = seconds
                aspectThresholds = thresholds
            }
        }
    }
}