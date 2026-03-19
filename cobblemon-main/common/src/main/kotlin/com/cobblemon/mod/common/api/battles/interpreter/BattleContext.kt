/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battles.interpreter

import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.queryStructOf
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon

/**
 * The context of how a condition was created or changed during a battle.
 *
 * @author Segfault Guy
 * @since April 10th, 2023
 */
interface BattleContext {

    /** The effect or condition this [BattleContext] represents. */
    val id: String

    /** The turn this [BattleContext] was created. */
    val turn: Int

    /** The [BattleContext.Type] tied to this [BattleContext]. */
    val type: Type

    /** The [BattlePokemon] that directly or indirectly caused this [BattleContext]. */
    val origin: BattlePokemon?

    fun getStruct() = queryStructOf(
        "type" to { StringValue(type.name) },
        "origin" to { origin?.struct ?: DoubleValue(0.0) },
        "id" to { StringValue(id) },
        "turn_created" to { DoubleValue(turn.toDouble()) }
    )

    /**
     * The type of [BattleContext].
     *
     * @property damaging Whether a context of this type is a source of damage.
     * @property exclusive Whether only one context of this type can exist.
     *
     * @author Segfault Guy
     * @since April 10th, 2023
     */
    enum class Type(val damaging: Boolean, val exclusive: Boolean) {
        ITEM(true, true),
        STATUS(true, false),
        VOLATILE(true, false),
        HAZARD(true, false),
        WEATHER(true, true),
        ROOM(false, true),
        SPORT(false, false),
        TERRAIN(false, true),
        GRAVITY(false, true),
        TAILWIND(false, true),
        SCREEN(false, false),
        FAINT(false, false),
        BOOST(false, false),
        UNBOOST(false, false),
        MISC(false, false);
    }
}

data class BasicContext(
        override val id: String,
        override val turn: Int,
        override val type: BattleContext.Type,
        override val origin: BattlePokemon?
) : BattleContext

data class MissingContext(
        override val id: String = "error",
        override val turn: Int = 0,
        override val type: BattleContext.Type = BattleContext.Type.MISC,
        override val origin: BattlePokemon? = null
) : BattleContext
