/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.ai.strongBattleAI

import com.cobblemon.mod.common.api.abilities.Ability
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import java.util.UUID
import kotlin.collections.firstOrNull
import kotlin.collections.getOrDefault
import kotlin.collections.groupingBy
import kotlin.collections.lastOrNull
import kotlin.collections.map

class ActiveTracker {
    var currentRoom: String? = null
    var currentTerrain: String? = null
    var currentWeather: String? = null
    val alliedSide: TrackerSide = TrackerSide()
    val opponentSide: TrackerSide = TrackerSide()
    val isInitialized: Boolean
        get() = alliedSide.actors.isNotEmpty() && opponentSide.actors.isNotEmpty()

    fun initialize(alliedSide: BattleSide) {
        this.alliedSide.actors.clear()
        this.alliedSide.actors.addAll(alliedSide.actors.map { TrackerActor.initializeAlly(it) })
        this.opponentSide.actors.clear()
        this.opponentSide.actors.addAll(alliedSide.getOppositeSide().actors.map { TrackerActor.initializeOpponent(it) })
    }

    fun updateActiveState(alliedSide: BattleSide) {
        this.alliedSide.updateAlliedActiveState(alliedSide)
        this.opponentSide.updateOpponentActiveState(alliedSide.getOppositeSide())
    }
}

class TrackerSide {
    var screenCondition: String? = null
    var tailwindCondition: String? = null
    var sideHazards: List<String> = emptyList()

    val actors : MutableList<TrackerActor> = mutableListOf()
    val activePokemon: MutableList<TrackerPokemon>
        get() = actors.flatMap { it.activePokemon }.toMutableList()

    fun updateAlliedActiveState(side: BattleSide) {
        actors.associateWith { side.actors.first { actor -> actor.uuid == it.uuid} }
            .forEach { (trackerActor, actor) -> trackerActor.updateAlliedActivePokemon(actor)}

        val activePokemon = side.activePokemon.mapNotNull { it.battlePokemon }
        for (pokemon in activePokemon) {
            val trackMon = this.activePokemon.first { it.id == pokemon.uuid }

            val boostPerStat = pokemon.contextManager.get(BattleContext.Type.BOOST)?.groupingBy { it.id }
                ?.eachCount()?.mapKeys { Stats.getStat(it.key) } ?: emptyMap()
            val unboostPerStat = pokemon.contextManager.get(BattleContext.Type.UNBOOST)?.groupingBy { it.id }
                ?.eachCount()?.mapKeys { Stats.getStat(it.key) } ?: emptyMap()

            trackMon.boosts.putAll(Stats.ALL.filterNot {it == Stats.HP}
                .associateWith { stat ->  boostPerStat.getOrDefault(stat, 0) - unboostPerStat.getOrDefault(stat, 0) })
            trackMon.currentStatus = pokemon.effectedPokemon.status?.status?.showdownName ?: pokemon.contextManager.get(BattleContext.Type.STATUS)?.lastOrNull()?.id
            trackMon.currentVolatile = pokemon.contextManager.get(BattleContext.Type.VOLATILE)?.lastOrNull()?.id
            trackMon.pokemon = pokemon.effectedPokemon
            trackMon.species = pokemon.effectedPokemon.species
            trackMon.form = pokemon.effectedPokemon.form
            trackMon.currentHp = pokemon.maxHealth
            trackMon.currentHpPercent = pokemon.health.toDouble() / pokemon.maxHealth.toDouble()
            trackMon.currentAbility = pokemon.effectedPokemon.ability
            trackMon.moves = pokemon.effectedPokemon.moveSet.toList()
        }
        //TODO count turns where applicable?
        sideHazards = side.contextManager.get(BattleContext.Type.HAZARD)?.map { it.id } ?: emptyList()
        tailwindCondition = side.contextManager.get(BattleContext.Type.TAILWIND)?.firstOrNull()?.id
        screenCondition = side.contextManager.get(BattleContext.Type.SCREEN)?.firstOrNull()?.id
    }

    fun updateOpponentActiveState(side: BattleSide) {
        actors.associateWith { side.actors.first { actor -> actor.uuid == it.uuid} }
            .forEach { (trackerActor, actor) -> trackerActor.updateOpponentActivePokemon(actor)}

        val activePokemon = side.activePokemon.mapNotNull { it.battlePokemon }
        for (pokemon in activePokemon) {
            val trackMon = this.activePokemon.first { it.id == pokemon.uuid }

            val boostPerStat = pokemon.contextManager.get(BattleContext.Type.BOOST)?.groupingBy { it.id }
                ?.eachCount()?.mapKeys { Stats.getStat(it.key) } ?: emptyMap()
            val unboostPerStat = pokemon.contextManager.get(BattleContext.Type.UNBOOST)?.groupingBy { it.id }
                ?.eachCount()?.mapKeys { Stats.getStat(it.key) } ?: emptyMap()

            trackMon.boosts.putAll(Stats.ALL.filterNot {it == Stats.HP}
                .associateWith { stat ->  boostPerStat.getOrDefault(stat, 0) - unboostPerStat.getOrDefault(stat, 0) })
            trackMon.currentStatus = pokemon.effectedPokemon.status?.status?.showdownName ?: pokemon.contextManager.get(BattleContext.Type.STATUS)?.lastOrNull()?.id
            trackMon.currentVolatile = pokemon.contextManager.get(BattleContext.Type.VOLATILE)?.lastOrNull()?.id
            val resetAbility = trackMon.species != (pokemon.entity?.exposedSpecies ?: pokemon.effectedPokemon.species) ||
                    trackMon.form != (pokemon.entity?.exposedForm ?: pokemon.effectedPokemon.form)
            trackMon.species = pokemon.entity?.exposedSpecies ?: pokemon.effectedPokemon.species
            trackMon.form = pokemon.entity?.exposedForm ?: pokemon.effectedPokemon.form
            if (trackMon.currentAbility != null || resetAbility) {
                trackMon.currentAbility = (trackMon.form?.abilities ?: trackMon.species!!.abilities).first().template.create() //educated guess, abilities of a pokemon are known
            }
            trackMon.currentHpPercent = pokemon.health.toDouble() / pokemon.maxHealth.toDouble()
        }
        //TODO count turns where applicable?
        sideHazards = side.contextManager.get(BattleContext.Type.HAZARD)?.map { it.id } ?: emptyList()
        tailwindCondition = side.contextManager.get(BattleContext.Type.TAILWIND)?.firstOrNull()?.id
        screenCondition = side.contextManager.get(BattleContext.Type.SCREEN)?.firstOrNull()?.id
    }
}

// Tracker Actor with a Party of Tracker Pokemon in a Party
class TrackerActor(val uuid: UUID) {
    val party: MutableList<TrackerPokemon> = mutableListOf()
    val activePokemon: MutableList<TrackerPokemon> = mutableListOf()
    val remainingMons: Int
        get() = (party + activePokemon).count { (it.currentHp?.toDouble() ?: it.currentHpPercent) > 0 }

    fun updateAlliedActivePokemon(actor: BattleActor) {
        actor.activePokemon.filter { newActive -> activePokemon.none { it.id == newActive.battlePokemon?.uuid } }.forEach {
            val active = party.firstOrNull { trackMon -> trackMon.id == it.battlePokemon?.uuid } ?: return@forEach
            party.remove(active)
            active.firstTurn = true // Reset firstTurn when switching in
            activePokemon.add(active)
        }
        val swappedOut = activePokemon.filter { trackMon -> actor.activePokemon.none { it.battlePokemon?.uuid == trackMon.id } }
        swappedOut.forEach {
            activePokemon.remove(it)
            party.add(it)
        }
    }

    fun updateOpponentActivePokemon(actor: BattleActor) {
        actor.activePokemon.filter { newActive -> activePokemon.none { it.id == newActive.battlePokemon?.uuid } }.forEach {
            val active = party.firstOrNull { trackMon -> trackMon.id == it.battlePokemon?.uuid } ?: return@forEach
            party.remove(active)
            active.firstTurn = true // Reset firstTurn when switching in
            activePokemon.add(active)
        }
        val swappedOut = activePokemon.filter { trackMon -> actor.activePokemon.none { it.battlePokemon?.uuid == trackMon.id } }
        swappedOut.forEach {
            activePokemon.remove(it)
            party.add(it)
        }
    }

    companion object {
        fun initializeAlly(actor: BattleActor) : TrackerActor {
            val trackerActor = TrackerActor(actor.uuid)
            trackerActor.party.clear()
            //we assume that AI knows it all about our allies
            trackerActor.party.addAll(actor.pokemonList.map { createAllied(it) })
            actor.activePokemon.forEach {
                val active = trackerActor.party.first { trackMon -> trackMon.id == it.battlePokemon!!.uuid }
                trackerActor.party.remove(active)
                trackerActor.activePokemon.add(active)
            }
            return trackerActor
        }

        fun initializeOpponent(actor: BattleActor) : TrackerActor {
            val trackerActor = TrackerActor(actor.uuid)
            trackerActor.party.clear()
            //only know basics of what we see right now
            trackerActor.party.addAll(actor.pokemonList.map { createOpposing(it) })
            actor.activePokemon.forEach {
                val active = trackerActor.party.first { trackMon -> trackMon.id == it.battlePokemon!!.uuid }
                trackerActor.party.remove(active)
                trackerActor.activePokemon.add(active)
            }
            return trackerActor
        }
    }
}

// Tracker Pokemon within the Party
data class TrackerPokemon(
    var id: UUID? = null,
    var pokemon: Pokemon? = null,
    var species: Species? = null,
    var form: FormData? = null,
    var currentHp: Int? = null,
    var currentHpPercent: Double = 0.0,
    var boosts: MutableMap<Stat, Int> = mutableMapOf(),
    var currentVolatile: String? = null,
    var currentStatus: String? = null,
    var currentAbility: Ability? = null, //TODO PICK UP FROM BATTLE ACTIVATION
    var moves: List<Move> = listOf(), //TODO PICK UP FROM BATTLE USEAGE
    var firstTurn: Boolean = true,
    var protectCount: Int = 0 //TODO MAYBE PICK UP FROM BATTLE USAGE
)

fun createAllied(pokemon: BattlePokemon) : TrackerPokemon {
    val trackerMon = TrackerPokemon()
    trackerMon.id = pokemon.uuid
    trackerMon.pokemon = pokemon.effectedPokemon
    trackerMon.species = pokemon.effectedPokemon.species
    trackerMon.form = pokemon.effectedPokemon.form
    trackerMon.currentHp = pokemon.maxHealth
    trackerMon.currentHpPercent = pokemon.health.toDouble() / pokemon.maxHealth.toDouble()
    trackerMon.currentAbility = pokemon.effectedPokemon.ability
    trackerMon.moves = pokemon.effectedPokemon.moveSet.toList()
    return trackerMon
}

fun createOpposing(pokemon: BattlePokemon) : TrackerPokemon {
    val trackerMon = TrackerPokemon()
    trackerMon.id = pokemon.uuid //mainly used as unique reference
    //everything else gets set on a "seen" basis
    return trackerMon
}