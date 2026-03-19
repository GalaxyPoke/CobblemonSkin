/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.advancement.CobblemonCriteria
import com.cobblemon.mod.common.advancement.criterion.*
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.battles.model.actor.ActorType
import com.cobblemon.mod.common.api.events.CobblemonEvents.BATTLE_VICTORY
import com.cobblemon.mod.common.api.events.CobblemonEvents.COLLECT_EGG
import com.cobblemon.mod.common.api.events.CobblemonEvents.EVOLUTION_COMPLETE
import com.cobblemon.mod.common.api.events.CobblemonEvents.HATCH_EGG_POST
import com.cobblemon.mod.common.api.events.CobblemonEvents.LEVEL_UP_EVENT
import com.cobblemon.mod.common.api.events.CobblemonEvents.POKEMON_CAPTURED
import com.cobblemon.mod.common.api.events.CobblemonEvents.RIDE_EVENT_POST
import com.cobblemon.mod.common.api.events.CobblemonEvents.TRADE_EVENT_POST
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.api.events.pokemon.*
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionCompleteEvent
import com.cobblemon.mod.common.block.TumblestoneBlock
import com.cobblemon.mod.common.item.TumblestoneItem
import com.cobblemon.mod.common.platform.events.ServerPlayerEvent
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes
import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.cobblemon.mod.common.util.effectiveName
import com.cobblemon.mod.common.util.getPlayer
import java.util.*

object AdvancementHandler : EventHandler {
    override fun registerListeners() {
        PlatformEvents.RIGHT_CLICK_BLOCK.subscribe(Priority.LOWEST, ::onTumbleStonePlaced)
        POKEMON_CAPTURED.subscribe(Priority.LOWEST, ::onCapture)
        BATTLE_VICTORY.subscribe(Priority.LOWEST, ::onWinBattle)
        EVOLUTION_COMPLETE.subscribe(Priority.LOWEST, ::onEvolve)
        LEVEL_UP_EVENT.subscribe(Priority.LOWEST, ::onLevelUp)
        TRADE_EVENT_POST.subscribe(Priority.LOWEST, ::onTradeCompleted)
        RIDE_EVENT_POST.subscribe(Priority.LOWEST, ::startRiding)
        HATCH_EGG_POST.subscribe(Priority.LOWEST, ::onHatch)
        COLLECT_EGG.subscribe(Priority.LOWEST, ::onEggCollect)
    }

    fun onCapture(event : PokemonCapturedEvent) {
        val playerData = Cobblemon.playerDataManager.getGenericData(event.player)
        val advancementData = playerData.advancementData
        advancementData.updateTotalCaptureCount()
        advancementData.updateAspectsCollected(event.player, event.pokemon)
        CobblemonCriteria.CATCH_POKEMON.trigger(event.player, CountablePokemonTypeContext(advancementData.totalCaptureCount, "any"/*, event.pokemon.species.resourceIdentifier.toString()*/))
        event.pokemon.types.forEach {
            advancementData.updateTotalTypeCaptureCount(it)
            CobblemonCriteria.CATCH_POKEMON.trigger(event.player, CountablePokemonTypeContext(advancementData.getTotalTypeCaptureCount(it), it.showdownId/*, event.pokemon.species.resourceIdentifier.toString()*/))
        }
        if (event.pokemon.shiny) {
            advancementData.updateTotalShinyCaptureCount()
            CobblemonCriteria.CATCH_SHINY_POKEMON.trigger(event.player, CountableContext(advancementData.totalShinyCaptureCount))
        }
        CobblemonCriteria.COLLECT_ASPECT.trigger(event.player, advancementData.aspectsCollected)
        Cobblemon.playerDataManager.saveSingle(playerData, PlayerInstancedDataStoreTypes.GENERAL)
    }

    fun onEggCollect(event: CollectEggEvent) {
        if (!event.isCanceled) {
            val playerData = Cobblemon.playerDataManager.getGenericData(event.player)
            val advancementData = playerData.advancementData
            advancementData.updateTotalEggsCollected()
            Cobblemon.playerDataManager.saveSingle(playerData, PlayerInstancedDataStoreTypes.GENERAL)
            CobblemonCriteria.EGG_COLLECT.trigger(event.player, CountableContext(advancementData.totalEggsCollected))
        }
    }

    fun onHatch(event: HatchEggEvent.Post) {
        val playerData = Cobblemon.playerDataManager.getGenericData(event.player)
        val advancementData = playerData.advancementData
        advancementData.updateTotalEggsHatched()
        Cobblemon.playerDataManager.saveSingle(playerData, PlayerInstancedDataStoreTypes.GENERAL)
        CobblemonCriteria.EGG_HATCH.trigger(event.player, CountableContext(advancementData.totalEggsHatched))
    }

    fun onEvolve(event: EvolutionCompleteEvent) {
        val player = event.pokemon.getOwnerPlayer()
        if (player != null) {
            if (event.pokemon.preEvolution != null) {
                val playerData = Cobblemon.playerDataManager.getGenericData(player)
                val advancementData = playerData.advancementData
                advancementData.updateTotalEvolvedCount()
                advancementData.updateAspectsCollected(player, event.pokemon)
                Cobblemon.playerDataManager.saveSingle(playerData, PlayerInstancedDataStoreTypes.GENERAL)
                CobblemonCriteria.EVOLVE_POKEMON.trigger(
                    player, EvolvePokemonContext(
                        event.pokemon.preEvolution!!.species.resourceIdentifier,
                        event.pokemon.species.resourceIdentifier,
                        advancementData.totalEvolvedCount
                    )
                )
                CobblemonCriteria.COLLECT_ASPECT.trigger(player, advancementData.aspectsCollected)
            }
            else {
                Cobblemon.LOGGER.warn("Evolution triggered by ${player.effectiveName().string} has missing evolution data for ${event.pokemon.species.resourceIdentifier}. Incomplete evolution data: ${event.evolution.id}, please report to the datapack creator!")
            }
        }
    }

    fun onWinBattle(event: BattleVictoryEvent) {
        if(!event.wasWildCapture) {
            if (event.battle.isPvW) {
                event.winners
                    .flatMap { it.getPlayerUUIDs().mapNotNull(UUID::getPlayer) }
                    .forEach { player ->
                        val playerData = Cobblemon.playerDataManager.getGenericData(player)
                        val advancementData = playerData.advancementData
                        event.battle.actors.forEach { battleActor ->
                            if (!event.winners.contains(battleActor) && battleActor.type == ActorType.WILD) {
                                battleActor.pokemonList.forEach { battlePokemon ->
                                    advancementData.updateTotalDefeatedCount(battlePokemon.originalPokemon)
                                }
                            }
                        }
                        Cobblemon.playerDataManager.saveSingle(playerData, PlayerInstancedDataStoreTypes.GENERAL)
                        CobblemonCriteria.DEFEAT_POKEMON.trigger(player, CountableContext(advancementData.totalBattleVictoryCount))
                    }
            }
        }
        event.winners
            .flatMap { it.getPlayerUUIDs().mapNotNull(UUID::getPlayer) }
            .forEach { player ->
                val playerData = Cobblemon.playerDataManager.getGenericData(player)
                val advancementData = playerData.advancementData
                advancementData.updateTotalBattleVictoryCount()
                if (event.battle.isPvW)
                    advancementData.updateTotalPvWBattleVictoryCount()
                if (event.battle.isPvP)
                    advancementData.updateTotalPvPBattleVictoryCount()
                if (event.battle.isPvN)
                    advancementData.updateTotalPvNBattleVictoryCount()
                Cobblemon.playerDataManager.saveSingle(playerData, PlayerInstancedDataStoreTypes.GENERAL)
                CobblemonCriteria.WIN_BATTLE.trigger(player, BattleCountableContext(event.battle, advancementData.totalBattleVictoryCount))
            }

    }

    fun onLevelUp(event : LevelUpEvent) {
        event.pokemon.getOwnerPlayer()?.let { CobblemonCriteria.LEVEL_UP.trigger(it, LevelUpContext(event.newLevel, event.pokemon)) }
    }

    fun onTradeCompleted(event : TradeEvent.Post) {
        val player1 = event.tradeParticipant1Pokemon.getOwnerPlayer()
        val player2 = event.tradeParticipant2Pokemon.getOwnerPlayer()
        if (player1 != null) {
            CobblemonCriteria.TRADE_POKEMON.trigger(player1, TradePokemonContext(event.tradeParticipant1Pokemon, event.tradeParticipant2Pokemon))
            val playerData = Cobblemon.playerDataManager.getGenericData(player1)
            val advancementData = playerData.advancementData
            advancementData.updateTotalTradedCount()
            advancementData.updateAspectsCollected(player1, event.tradeParticipant2Pokemon)
            CobblemonCriteria.COLLECT_ASPECT.trigger(player1, advancementData.aspectsCollected)
            Cobblemon.playerDataManager.saveSingle(playerData, PlayerInstancedDataStoreTypes.GENERAL)
        }
        if (player2 != null) {
            CobblemonCriteria.TRADE_POKEMON.trigger(player2, TradePokemonContext(event.tradeParticipant2Pokemon, event.tradeParticipant1Pokemon))
            val playerData = Cobblemon.playerDataManager.getGenericData(player2)
            val advancementData = playerData.advancementData
            advancementData.updateTotalTradedCount()
            advancementData.updateAspectsCollected(player2, event.tradeParticipant1Pokemon)
            CobblemonCriteria.COLLECT_ASPECT.trigger(player2, advancementData.aspectsCollected)
            Cobblemon.playerDataManager.saveSingle(playerData, PlayerInstancedDataStoreTypes.GENERAL)
        }
    }

    /**
     * Triggers the advancement for placing a tumblestone
     *
     * Checks if the item in the player's hand is a tumblestone.
     * Then gets the block as a [TumblestoneBlock] so we can call the canGrow method.
     * Finally, triggers the advancement.
     *
     * @param event the event to trigger the advancement from
     */
    fun onTumbleStonePlaced(event: ServerPlayerEvent.RightClickBlock) {
        if (event.player.getItemInHand(event.hand).item is TumblestoneItem) {
            val block = ((event.player.getItemInHand(event.hand).item as TumblestoneItem).block as TumblestoneBlock)
            CobblemonCriteria.PLANT_TUMBLESTONE.trigger(event.player, PlantTumblestoneContext(event.pos, block))
        }
    }

    fun startRiding(event: RidePokemonEvent.Post) {
        CobblemonCriteria.RIDING_STAT_BOOST.trigger(event.player, RidingStatBoostContext(event.pokemon))
    }
}
