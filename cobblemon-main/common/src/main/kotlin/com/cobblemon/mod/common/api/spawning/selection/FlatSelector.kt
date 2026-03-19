/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.selection

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.util.removeIf
import com.cobblemon.mod.common.util.weightedSelection
import kotlin.random.Random

/**
 * A spawning selector that compiles a distinct list of all spawn details that
 * are possible across any spawnable position type and performs a weighted selection of spawns.
 *
 * The goal of this algorithm is to be kinder to spawns that are only possible
 * in very specific locational conditions or spawnable position types by not letting the scarcity
 * of suitable locations hurt its chances of spawning. It differs from the
 * [FlatSpawnablePositionWeightedSelector] because here a spawnable position type only appearing once
 * will not make it any less likely to be selected.
 *
 * A spawn detail's weight when selecting between all possible spawn details will be the
 * highest of its spawn position-adjusted weights. In other words, if a spawn is possible at both A and
 * B spawnable positions but is weighed more highly at A due to a weight multiplier, it will use the
 * weight of the spawn at A when choosing which spawn should be picked.
 *
 * When choosing which spawnable position a specific spawn detail will spawn at once that spawn detail
 * has been chosen, it does a spawnable position-weighted random selection across those spawnable positions.
 *
 * At a glance:
 * - Spawn detail selection is flat across spawnable position quantity
 * - The popularity of specific spawnable position types has no bearing on the spawn chance.
 *
 * @author Hiroku
 * @since July 10th, 2022
 */
open class FlatSelector : SpawningSelector<FlatSelector.SpawnablePositionSelectionData> {
    class SelectingSpawnInformation {
        val spawnablePositions = mutableMapOf<SpawnablePosition, Float>()
        var highestWeight = 0F

        fun add(spawnablePosition: SpawnablePosition, spawnablePositionWeight: Float) {
            spawnablePositions[spawnablePosition] = spawnablePositionWeight
            if (spawnablePositionWeight > highestWeight) {
                highestWeight = spawnablePositionWeight
            }
        }

        fun chooseSpawnablePosition() = spawnablePositions.entries.weightedSelection { it.value }!!.key
    }

    class SpawnablePositionSelectionData(
        val spawnToSpawnablePosition: MutableMap<SpawnDetail, SelectingSpawnInformation>,
        var percentSum: Float
    ): SpawnSelectionData {
        val size: Int
            get() = spawnToSpawnablePosition.size

        override val spawnActions = mutableListOf<SpawnAction<*>>()
        override val context = mutableMapOf<String, Any>()

        override fun removeSpawnDetails(shouldRemove: (SpawnDetail) -> Boolean) {
            val toRemove = spawnToSpawnablePosition.entries.filter { shouldRemove(it.key) }
            toRemove.forEach { spawnToSpawnablePosition.remove(it.key) }
            percentSum -= toRemove.sumOf { it.key.percentage.toDouble().takeIf { it > 0 } ?: 0.0 }.toFloat()
        }

        override fun removeSpawnablePositions(shouldRemove: (SpawnDetail, SpawnablePosition) -> Boolean) {
            val toRemove = spawnToSpawnablePosition.entries.filter { (spawnDetail, positionData) ->
                positionData.spawnablePositions.removeIf { shouldRemove(spawnDetail, it.key) }
                positionData.highestWeight = positionData.spawnablePositions.maxOfOrNull { it.value } ?: 0F
                positionData.spawnablePositions.isEmpty()
            }

            toRemove.forEach { spawnToSpawnablePosition.remove(it.key) }

            percentSum -= toRemove.sumOf { it.key.percentage.toDouble().takeIf { it > 0 } ?: 0.0 }.toFloat()
        }
    }

    override fun getSelectionData(
        spawner: Spawner,
        bucket: SpawnBucket,
        spawnablePositions: List<SpawnablePosition>
    ): SpawnablePositionSelectionData {
        val spawnToSpawnablePosition: MutableMap<SpawnDetail, SelectingSpawnInformation> = mutableMapOf()
        var percentSum = 0F

        spawnablePositions.forEach { spawnablePosition ->
            spawner.getMatchingSpawns(bucket, spawnablePosition).forEach {
                // Only add to percentSum if this is the first time we've seen this SpawnDetail, otherwise
                // the percentage will get amplified for every spawnable position the thing was possible, completely
                // ruining the point of this pre-selection percentage.
                if (it.percentage > 0 && !spawnToSpawnablePosition.containsKey(it)) {
                    percentSum += it.percentage
                }

                val selectingSpawnInformation = spawnToSpawnablePosition.getOrPut(
                    it,
                    ::SelectingSpawnInformation
                )
                selectingSpawnInformation.add(spawnablePosition, spawnablePosition.getWeight(it))
            }
        }

        return SpawnablePositionSelectionData(spawnToSpawnablePosition, percentSum)
    }

    override fun selectSpawnAction(
        spawner: Spawner,
        bucket: SpawnBucket,
        selectionData: SpawnablePositionSelectionData,
    ): SpawnAction<*>? {
        val spawnToSpawnablePosition = selectionData.spawnToSpawnablePosition
        var percentSum = selectionData.percentSum

        // First pass is doing percentage checks.
        if (percentSum > 0) {

            if (percentSum > 100) {
                Cobblemon.LOGGER.warn(
                    """
                        A spawn list for ${spawner.name} exceeded 100% on percentage sums...
                        This means you don't understand how this option works.
                    """.trimIndent()
                )
                return null
            }

            /*
             * It's [0, 1) and I want (0, 1]
             * See half-open intervals here https://en.wikipedia.org/wiki/Interval_(mathematics)#Terminology
             */
            val selectedPercentage = 100 - Random.Default.nextFloat() * 100
            percentSum = 0F
            for ((spawnDetail, info) in spawnToSpawnablePosition) {
                if (spawnDetail.percentage > 0) {
                    percentSum += spawnDetail.percentage
                    if (percentSum >= selectedPercentage) {
                        return spawnDetail.choose(
                            spawnablePosition = info.chooseSpawnablePosition(),
                            bucket = bucket,
                            selectionData = selectionData
                        )
                    }
                }
            }
        }

        val selectedSpawn = spawnToSpawnablePosition.entries.toList().weightedSelection { it.value.highestWeight }!!
        return selectedSpawn.key.choose(
            spawnablePosition = selectedSpawn.value.chooseSpawnablePosition(),
            bucket = bucket,
            selectionData = selectionData
        )
    }

    override fun getTotalWeights(
        spawner: Spawner,
        bucket: SpawnBucket,
        spawnablePositions: List<SpawnablePosition>
    ): Map<SpawnDetail, Float> {
        val selectionData = getSelectionData(spawner, bucket, spawnablePositions)

        if (selectionData.size == 0) {
            return emptyMap()
        }

        val totalWeights = mutableMapOf<SpawnDetail, Float>()

        for ((spawnDetail, info) in selectionData.spawnToSpawnablePosition.entries) {
            totalWeights[spawnDetail] = info.highestWeight
        }

        return totalWeights
    }
}