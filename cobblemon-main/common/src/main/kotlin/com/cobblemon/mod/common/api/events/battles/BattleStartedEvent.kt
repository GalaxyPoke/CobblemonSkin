/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.battles

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.moLangFunctionMap
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.util.asArrayValue
import net.minecraft.network.chat.MutableComponent

/**
 * Events regarding the starting of a [PokemonBattle].
 *
 * @author Segfault Guy/MeAlam
 * @since March 26th 2023
 */
interface BattleStartedEvent : BattleEvent {
    /**
     * The battle that is starting.
     */
    override val battle: PokemonBattle

    /**
     * Event fired before a [PokemonBattle] is started. Canceling this event prevents the battle from being
     * created and launched.
     *
     * @property reason The text used to inform the participants why the battle was canceled. Keep null for a default error.
     *
     * @author Segfault Guy/MeAlam
     * @since March 26th 2023
     */
    data class Pre(
        override val battle: PokemonBattle,
        var reason: MutableComponent? = null
    ) : BattleStartedEvent, Cancelable() {
        val context = mutableMapOf(
            "battle" to battle.struct,
            "players" to battle.actors.filter { it.type == ActorType.PLAYER }.asArrayValue { it.struct },
            "npcs" to battle.actors.filter { it.type == ActorType.NPC }.asArrayValue { it.struct },
            "wild_pokemon" to battle.actors.filter { it.type == ActorType.WILD }.asArrayValue { it.struct }
        )

        val functions = moLangFunctionMap(cancelFunc)
        
        fun getBattleSide(side: Int) : BattleSide? {
            return when (side) {
                1 -> battle.side1
                2 -> battle.side2
                else -> null
            }
        }
        
        fun getPokemonOnSide(side: Int) : List<ActiveBattlePokemon>? {
            val battleSide = getBattleSide(side)
            val activePokemon = battleSide?.activePokemon
            return activePokemon
        }
    }

    /**
     * Event fired after a [PokemonBattle] starts.
     *
     * @author Segfault Guy/MeAlam
     * @since March 26th 2023
     */
    data class Post(
        override val battle: PokemonBattle
    ) : BattleStartedEvent {
        val context = mutableMapOf(
            "battle" to battle.struct,
            "players" to battle.actors.filter { it.type == ActorType.PLAYER }.asArrayValue { it.struct },
            "npcs" to battle.actors.filter { it.type == ActorType.NPC }.asArrayValue { it.struct },
            "wild_pokemon" to battle.actors.filter { it.type == ActorType.WILD }.asArrayValue { it.struct }
        )
    }

}