/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon

import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.moLangFunctionMap
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Fired during a Hyper Training mutation.
 *
 * @see [Pre]
 * @see [Post]
 */
interface HyperTrainedIvEvent {

    /**
     * The [Stat] having whose IV is being hyper-trained.
     */
    val stat: Stat

    /**
     * The [Pokemon] being hyper-trained.
     */
    val pokemon: Pokemon

    /**
     * Fired before Hyper Training occurs, cancelling will cause the mutation to be flagged as not successful.
     */
    class Pre(override val pokemon: Pokemon,  override val stat: Stat, var value: Int) : HyperTrainedIvEvent, Cancelable() {
        val context = mutableMapOf(
            "pokemon" to pokemon.struct,
            "stat" to StringValue(stat.toString()),
            "value" to DoubleValue(value)
        )
        val functions = moLangFunctionMap(cancelFunc)
    }

    /**
     * Fired after Hyper Training occurs, this is purely for notification purposes.
     */
    class Post(override val pokemon: Pokemon, override val stat: Stat, val value: Int) : HyperTrainedIvEvent {
        val context = mutableMapOf(
            "pokemon" to pokemon.struct,
            "stat" to StringValue(stat.toString()),
            "value" to DoubleValue(value)
        )
    }
}