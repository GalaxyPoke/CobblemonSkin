/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.battles

import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.cobblemon.mod.common.entity.MoLangScriptingEntity
import com.cobblemon.mod.common.entity.npc.NPCBattleActor
import com.cobblemon.mod.common.util.asArrayValue
import net.minecraft.world.entity.LivingEntity

/**
 * Event fired when a battle is won by some number of [BattleActor]s.
 *
 * @author MoeBoy76
 * @since November 3rd, 2022
 */
data class BattleVictoryEvent (
    override val battle: PokemonBattle,
    val winners : List<BattleActor>,
    val losers : List<BattleActor>,
    val wasWildCapture : Boolean
) : BattleEvent {
    val context = mutableMapOf<String, MoValue>(
        "battle" to battle.struct,
        "winners" to winners.asArrayValue { it.struct },
        "losers" to losers.asArrayValue { it.struct },
        "was_wild_capture" to DoubleValue(wasWildCapture),
        "player_winners" to winners.filter { it.type == ActorType.PLAYER }.asArrayValue { it.struct },
        "player_losers" to losers.filter { it.type == ActorType.PLAYER }.asArrayValue { it.struct },
        "npc_winners" to winners.filter { it.type == ActorType.NPC }.asArrayValue { it.struct },
        "npc_losers" to losers.filter { it.type == ActorType.NPC }.asArrayValue { it.struct },
        "wild_pokemon_winners" to winners.filter { it.type == ActorType.WILD }.asArrayValue { it.struct },
        "wild_pokemon_losers" to losers.filter { it.type == ActorType.WILD }.asArrayValue { it.struct },
        "scriptable_winners" to winners.filterIsInstance<EntityBackedBattleActor<*>>().mapNotNull { it.entity as? MoLangScriptingEntity }.asArrayValue { (it as LivingEntity).asMostSpecificMoLangValue() },
        "scriptable_losers" to losers.filterIsInstance<EntityBackedBattleActor<*>>().mapNotNull { it.entity as? MoLangScriptingEntity }.asArrayValue { (it as LivingEntity).asMostSpecificMoLangValue() },
        "players" to battle.actors.filter { it.type == ActorType.PLAYER }.asArrayValue { it.struct },
        "npcs" to battle.actors.filter { it.type == ActorType.NPC }.asArrayValue { it.struct },
        "wild_pokemon" to battle.actors.filter { it.type == ActorType.WILD }.asArrayValue { it.struct }
    )
}