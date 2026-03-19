/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.EvGainedEvent
import com.cobblemon.mod.common.api.pokemon.stats.EvSource
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import kotlin.math.min

class EVs : PokemonStats() {
    override val acceptableRange = 0..MAX_STAT_VALUE
    override val defaultValue = 0

    override fun canSet(stat: Stat, value: Int): Boolean {
        if (!super.canSet(stat, value)) {
            return false
        }
        val simulated = this.associate { it.key to it.value }.toMutableMap()
        simulated[stat] = value
        val simulatedTotal = simulated.values.sum()
        return simulatedTotal <= MAX_TOTAL_VALUE
    }

    /**
     * Safely adds the given amount of EVs to this store.
     *
     * @param key The [Stat] being mutated.
     * @param value The amount to attempt to add, this wil safely be coerced to the highest possible value.
     * @return The amount added or 0 if the addition was impossible.
     */
    @Deprecated(
        message = "Please update to modern API",
        replaceWith = ReplaceWith("add(key, value, source)", "com.cobblemon.mod.common.api.pokemon.stats.Stat", "com.cobblemon.mod.common.api.pokemon.stats.EvSource"),
        level = DeprecationLevel.ERROR
    )
    fun add(key: Stat, value: Int): Int {
        return this.performAdd(key, value)
    }

    /**
     * Safely adds the given amount of EVs to this store.
     *
     * @param key The [Stat] being mutated.
     * @param value The amount to attempt to add, this wil safely be coerced to the highest possible value.
     * @param source The [EvSource] of this addition.
     * @return The amount added or 0 if the addition was impossible.
     */
    fun add(key: Stat, value: Int, source: EvSource): Int {
        CobblemonEvents.EV_GAINED_EVENT_PRE.postThen(
            event = EvGainedEvent.Pre(key, value, source),
            ifCanceled = { return 0 },
            ifSucceeded = { event ->
                val result = this.performAdd(event.stat, event.amount)
                CobblemonEvents.EV_GAINED_EVENT_POST.post(EvGainedEvent.Post(key, result, source))
                return result
            }
        )
        // This never runs, just the impl of the observables doesn't allow us to infer the actual return
        return 0
    }

    private fun performAdd(stat: Stat, value: Int): Int {
        val currentTotal = this.sumOf { it.value }
        if (currentTotal == MAX_TOTAL_VALUE && value > 0) {
            return 0
        }
        val currentStat = this.getOrDefault(stat)
        val possibleForStat = MAX_STAT_VALUE - currentStat
        val possibleForTotal = MAX_TOTAL_VALUE - currentTotal
        val coercedValue = value.coerceIn(-currentStat, min(possibleForStat, possibleForTotal))
        val newValue = currentStat + coercedValue
        // avoid unnecessary updates
        if (newValue != currentStat) {
            this[stat] = newValue
            return coercedValue
        }
        return 0
    }

    companion object {
        const val MAX_STAT_VALUE = 252
        const val MAX_TOTAL_VALUE = 510

        @JvmStatic
        fun createEmpty(): EVs = Cobblemon.statProvider.createEmptyEVs()

        @JvmStatic
        val CODEC: Codec<EVs> = Codec.unboundedMap(Stat.PERMANENT_ONLY_CODEC, Codec.intRange(0, MAX_STAT_VALUE))
            .comapFlatMap(
                { map ->
                    if (map.values.sum() > MAX_TOTAL_VALUE) {
                        return@comapFlatMap DataResult.error { "EVs cannot exceed a total of $MAX_TOTAL_VALUE" }
                    }
                    val evs = Cobblemon.statProvider.createEmptyEVs()
                    map.forEach { (stat, value) -> evs[stat] = value }
                    return@comapFlatMap DataResult.success(evs)
                },
                { ivs ->
                    val map = hashMapOf<Stat, Int>()
                    ivs.forEach { (stat, value) -> map[stat] = value }
                    return@comapFlatMap map
                }
            )

        @JvmStatic
        val STREAM_CODEC: StreamCodec<ByteBuf, EVs> = ByteBufCodecs.fromCodec(CODEC)
    }
}