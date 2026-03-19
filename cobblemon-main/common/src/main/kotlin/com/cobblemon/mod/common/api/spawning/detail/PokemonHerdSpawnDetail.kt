/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.detail

import com.cobblemon.mod.common.api.drop.DropTable
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.api.spawning.selection.SpawnSelectionData
import com.cobblemon.mod.common.util.weightedSelection
import com.google.gson.annotations.SerializedName

/**
 * A spawn detail that spawns a herd of Pokémon. This relies on the same [PokemonSpawnAction]s as
 * regular [PokemonSpawnDetail]s, but takes advantage of selection context to 'take over' the spawn
 * pass and spawn many for the price of one.
 *
 * Selection will treat this like a single spawn until it chooses this detail once, at which point
 * all the other spawns are removed, a level is calculated for the herd based on [levelRange], and
 * the selector will be left to fit in as many herd members as possible with the remaining spawnable
 * positions.
 *
 * @author Hiroku
 * @since April 22nd, 2025
 */
class PokemonHerdSpawnDetail : SpawnDetail() {
    companion object {
        val TYPE = "pokemon-herd"
        const val HERD_LEADER = "leader"
    }

    override val type: String = TYPE
    var herdablePokemon = mutableListOf<Herdable>()
    @SerializedName("level", alternate = ["levelRange"])
    var levelRange: IntRange = 1..100
    var maxHerdSize = 10
    var minDistanceBetweenSpawns = 1F

    val herdLevelKey: String
        get() = "${id}__LEVEL"

    class Herdable {
        var pokemon: PokemonProperties = PokemonProperties()
        val levelRange: IntRange? = null
        var levelRangeOffset: IntRange? = null
        var dropTable: DropTable? = null
        var isLeader: Boolean? = null
        var weight: Float = 1F
        var maxTimes = 10
    }

    override fun isValid() = super.isValid() && herdablePokemon.all { it.pokemon.hasSpecies() && it.weight > 0F && it.maxTimes > 0 }
    override fun onSelection(
        spawnablePosition: SpawnablePosition,
        spawnAction: SpawnAction<*>,
        selectionData: SpawnSelectionData
    ) {
        selectionData.removeSpawnDetails { it != this }
        selectionData.removeSpawnablePositions { _, pos -> pos.distanceTo(spawnablePosition) < minDistanceBetweenSpawns }

        val herdSpawnCount = selectionData.spawnActions.count { it.detail == this } + 1

        if (herdSpawnCount >= maxHerdSize || getValidHerdMembers(selectionData).isEmpty()) {
            // We can't spawn any more in the herd so remove this spawn from the selection pool.
            selectionData.removeSpawnDetails { it == this }
        }
    }

    fun lacksPossibleLeader(selectionData: SpawnSelectionData): Boolean {
        val level = getOrSetHerdLevel(selectionData)
        val herdable = getValidHerdMembers(selectionData)
        val herdCounts = getHerdMemberCounts(selectionData)

        val leaderIsPossible = herdable.any { it.levelRange?.contains(level) != false && it.isLeader == true }
        val leaderIsSelected = herdCounts.any { it.key?.isLeader == true && it.value > 0 }
        return leaderIsPossible && !leaderIsSelected
    }

    private fun getOrSetHerdLevel(selectionData: SpawnSelectionData): Int {
        return (selectionData.context[herdLevelKey] as? Int) ?: run {
            val level = levelRange.random()
            selectionData.context[herdLevelKey] = level
            return level
        }
    }

    override fun createSpawnAction(
        spawnablePosition: SpawnablePosition,
        bucket: SpawnBucket,
        selectionData: SpawnSelectionData
    ): PokemonSpawnAction {
        val validHerdMembers = getValidHerdMembers(selectionData)
        val herdable = if (lacksPossibleLeader(selectionData)) {
            validHerdMembers.filter { it.isLeader == true }
        } else {
            validHerdMembers
        }.weightedSelection { it.weight } ?: herdablePokemon[0]

        val level = selectionData.context[herdLevelKey] as? Int ?: 1
        val levelRange = herdable.levelRangeOffset?.let { offset ->
            val min = level + offset.first
            val max = level + offset.last
            min..max
        } ?: level..level

        return PokemonSpawnAction(
            spawnablePosition = spawnablePosition,
            bucket = bucket,
            detail = this,
            props = herdable.pokemon,
            drops = herdable.dropTable,
            heldItem = null,
            levelRange = levelRange
        ).also {
            if (herdable.isLeader == true) {
                it.labels += HERD_LEADER
            }
        }
    }

    fun getHerdMemberCounts(selectionData: SpawnSelectionData): Map<Herdable?, Int> {
        val relevantActions = selectionData.spawnActions.filterIsInstance<PokemonSpawnAction>().filter { it.detail == this }
        return relevantActions.groupBy { action -> herdablePokemon.find { it.pokemon == action.props } }.mapValues { it.value.size }
    }

    private fun getValidHerdMembers(selectionData: SpawnSelectionData): List<Herdable> {
        val level = getOrSetHerdLevel(selectionData)
        val relevantActions = selectionData.spawnActions.filterIsInstance<PokemonSpawnAction>().filter { it.detail == this }
        val counts = relevantActions.groupBy { action -> herdablePokemon.find { it.pokemon == action.props } }.mapValues { it.value.size }
        return herdablePokemon.filter { herdable ->
            val count = counts[herdable] ?: 0
            return@filter when {
                count >= herdable.maxTimes -> false
                herdable.levelRange != null && level !in herdable.levelRange -> false
                else -> true
            }
        }
    }
}