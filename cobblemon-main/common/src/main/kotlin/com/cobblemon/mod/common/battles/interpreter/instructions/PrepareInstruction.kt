/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter.instructions

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addBattleMessageFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addFunctions
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.api.moves.animations.ActionEffects
import com.cobblemon.mod.common.api.moves.animations.UsersProvider
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.battles.ShowdownInterpreter
import com.cobblemon.mod.common.battles.dispatch.ActionEffectInstruction
import com.cobblemon.mod.common.battles.dispatch.GO
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction
import com.cobblemon.mod.common.battles.dispatch.UntilDispatch
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.PoisonStatus
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.battleLang
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.concurrent.CompletableFuture
import net.minecraft.resources.ResourceLocation

/**
 * Format: |-prepare|ATTACKER|MOVE and |-prepare|ATTACKER|MOVE|DEFENDER
 *
 * ATTACKER Pokémon is preparing to use a charge MOVE on DEFENDER or an unknown target.
 * @author Renaissance
 * @since March 24th, 2023
 */
class PrepareInstruction(val message: BattleMessage): ActionEffectInstruction {
    override var future: CompletableFuture<*> = CompletableFuture.completedFuture(Unit)
    override var holds = mutableSetOf<String>()
    override val id = cobblemonResource("prepare")

    override fun addMolangQueries(runtime: MoLangRuntime) {
        super.addMolangQueries(runtime)
        runtime.environment.query.addBattleMessageFunctions(message)
    }

    override fun preActionEffect(battle: PokemonBattle) {
        val pokemon = message.battlePokemon(0, battle) ?: return
        val effect = message.effectAt(1) ?: return
        ShowdownInterpreter.broadcastOptionalAbility(battle, effect, pokemon)

        battle.dispatch{
            ShowdownInterpreter.lastCauser[battle.battleId] = message
            battle.minorBattleActions[pokemon.uuid] = message
            GO
        }
    }

    override fun runActionEffect(battle: PokemonBattle, runtime: MoLangRuntime) {
        val effect = message.effectAt(1)
        val battlePokemon = message.battlePokemon(0, battle) ?: return
        battle.dispatch {
            val actionEffect = effect?.let { ActionEffects.actionEffects["prepare_${it.id}".asIdentifierDefaultingNamespace()] }
                ?: return@dispatch GO // not likely

            val providers = mutableListOf<Any>(battle)
            battlePokemon.effectedPokemon.entity?.let { UsersProvider(it) }?.let(providers::add)

            val context = ActionEffectContext(
                actionEffect = actionEffect,
                runtime = runtime,
                providers = providers,
                level = battle.players.firstOrNull()?.level()
            )
            this.future = actionEffect.run(context)
            holds = context.holds // Reference so future things can check on this action effect's holds
            future.thenApply { holds.clear() }
            return@dispatch GO
        }
    }

    override fun postActionEffect(battle: PokemonBattle) {
        battle.dispatch {
            val pokemon = message.battlePokemon(0, battle) ?: return@dispatch GO
            val pokemonName = pokemon.getName()
            val effectID = message.effectAt(1)?.id ?: return@dispatch GO
            //Prevents spam when the move Role Play is used
            val lang = when (effectID) {
                "shadowforce" -> battleLang("prepare.phantomforce", pokemonName) //Phantom Force and Shadow Force share the same text
                "solarblade" -> battleLang("prepare.solarbeam", pokemonName) //Solar Beam and Solar Blade share the same text
                else -> battleLang("prepare.$effectID", pokemonName)
            }
            battle.broadcastChatMessage(lang)
            battle.minorBattleActions[pokemon.uuid] = message
            UntilDispatch { "effects" !in holds }
        }
    }
}