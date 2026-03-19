/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.battles

import com.bedrockk.molang.runtime.value.MoValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.util.asArrayValue

/**
 * Event fired when a [PokemonBattle] is fled by a [PlayerBattleActor].
 *
 * @author Segfault Guy
 * @since March 25th 2023
 */
data class BattleFledEvent (
    override val battle: PokemonBattle,
    val player: PlayerBattleActor
) : BattleEvent {
    val context = mutableMapOf<String, MoValue>(
        "battle" to battle.struct,
        "players" to battle.actors.filter { it.type == ActorType.PLAYER }.asArrayValue { it.struct },
        "npcs" to battle.actors.filter { it.type == ActorType.NPC }.asArrayValue { it.struct },
        "wild_pokemon" to battle.actors.filter { it.type == ActorType.WILD }.asArrayValue { it.struct }
    )
}