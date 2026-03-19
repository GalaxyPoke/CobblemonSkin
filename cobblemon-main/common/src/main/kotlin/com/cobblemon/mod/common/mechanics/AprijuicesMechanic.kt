/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mechanics

import com.cobblemon.mod.common.api.apricorn.Apricorn
import com.cobblemon.mod.common.api.riding.stats.RidingStat
import com.cobblemon.mod.common.client.pot.CookingQuality
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Mechanic to hold various properties that motivate aprijuice as a mechanic.
 *
 * @author Hiroku
 * @since November 7th, 2025
 */
class AprijuicesMechanic {
    /** The points that apply to different riding stats, based on the apricorn used for the aprijuice. */
    val apricornStatEffects = mutableMapOf<Apricorn, Map<RidingStat, Int>>()
    /** Maps flavour values to stat points for riding stats. Aprijuice finds the highest threshold it meets. */
    val statPointFlavourThresholds = mutableMapOf<Int, Int>()
    /** Maps stat points for riding stats to what cooking quality that represents. It's for tooltips. */
    val cookingQualityPointThresholds = mutableMapOf<Int, CookingQuality>()

    internal fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeMap(this.apricornStatEffects,
            { _, apricorn -> buffer.writeEnum(apricorn) },
            { _, ridingStats ->
                buffer.writeMap(ridingStats,
                    { _, ridingStat -> buffer.writeEnum(ridingStat) },
                    { _, value -> buffer.writeVarInt(value) }
                )
            }
        )

        buffer.writeMap(this.statPointFlavourThresholds,
            { _, key -> buffer.writeVarInt(key) },
            { _, value -> buffer.writeVarInt(value) }
        )

        buffer.writeMap(this.cookingQualityPointThresholds,
            { _, key -> buffer.writeVarInt(key) },
            { _, cookingQuality -> buffer.writeEnum(cookingQuality) }
        )
    }

    companion object {
        internal fun decode(buffer: RegistryFriendlyByteBuf): AprijuicesMechanic {
            val mechanic = AprijuicesMechanic()

            val decodedApricornStatEffects = buffer.readMap(
                { buffer.readEnum<Apricorn>(Apricorn::class.java) },
                {
                    buffer.readMap(
                        { buffer.readEnum<RidingStat>(RidingStat::class.java) },
                        { buffer.readVarInt() }
                    ).toMutableMap()
                }
            ).toMutableMap()

            mechanic.apricornStatEffects.clear()
            mechanic.apricornStatEffects.putAll(decodedApricornStatEffects)

            val decodedStatPointFlavourThresholds = buffer.readMap(
                { buffer.readVarInt() },
                { buffer.readVarInt() }
            )
            mechanic.statPointFlavourThresholds.clear()
            mechanic.statPointFlavourThresholds.putAll(decodedStatPointFlavourThresholds)

            val decodedCookingQualityPointThresholds = buffer.readMap(
                { buffer.readVarInt() },
                { buffer.readEnum(CookingQuality::class.java) }
            )
            mechanic.cookingQualityPointThresholds.clear()
            mechanic.cookingQualityPointThresholds.putAll(decodedCookingQualityPointThresholds)

            return mechanic
        }
    }
}