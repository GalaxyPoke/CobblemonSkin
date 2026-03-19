/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.util.DataKeys
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

class IVs : PokemonStats() {
    override val acceptableRange = 0..MAX_VALUE
    override val defaultValue = 0
    val hyperTrainedIVs = mutableMapOf<Stat, Int>()

    fun isHyperTrained(stat: Stat): Boolean {
        return hyperTrainedIVs.containsKey(stat)
    }

    fun setHyperTrainedIV(stat: Stat, value: Int) {
        if (value in acceptableRange) {
            if(value == this[stat]) {
                //not hypertrained any more if it ends up being the same as natural IV
                if (hyperTrainedIVs.remove(stat) == null)
                    return //this means nothing has changed; no hypertraining did actually happen, e.g. you hypertrain to 31 HP when your normal IVs are already 31, its moot so no update needed
            }
            else {
                hyperTrainedIVs[stat] = value
            }
            update()
        } else {
            throw IllegalArgumentException("Value $value is out of acceptable range $acceptableRange")
        }
    }

    // Used to get the value that should be used for stat calculation.
    fun getEffectiveBattleIV(stat: Stat): Int {
        return (this.hyperTrainedIVs[stat] ?: this[stat]) as Int
    }

    fun getEffectiveBattleTotal(): Int {
        var total = 0
        for(stat in Stats.PERMANENT) {
            total += getEffectiveBattleIV(stat)
        }
        return total
    }

    companion object {
        const val MAX_VALUE = 31
        const val MAX_TOTAL = 186

        @JvmStatic
        fun createRandomIVs(minPerfectIVs: Int = 0): IVs = Cobblemon.statProvider.createEmptyIVs(minPerfectIVs)

        @JvmStatic
        val CODEC: Codec<IVs> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.unboundedMap(Stat.PERMANENT_ONLY_CODEC, Codec.intRange(0, MAX_VALUE)).fieldOf(DataKeys.POKEMON_IVS_BASE).forGetter(IVs::stats),
                Codec.unboundedMap(Stat.PERMANENT_ONLY_CODEC, Codec.intRange(0, MAX_VALUE)).fieldOf(DataKeys.POKEMON_IVS_HYPERTRAINED).forGetter(IVs::hyperTrainedIVs)
            ).apply(instance) { stats, hyperTrained ->
                val ivs = IVs()
                stats.entries.forEach {
                    ivs[it.key] = it.value
                }
                hyperTrained.entries.forEach {
                    ivs.setHyperTrainedIV(it.key, it.value)
                }
                ivs
            }
        }

        @JvmStatic
        val STREAM_CODEC: StreamCodec<ByteBuf, IVs> = ByteBufCodecs.fromCodec(CODEC)
    }
}