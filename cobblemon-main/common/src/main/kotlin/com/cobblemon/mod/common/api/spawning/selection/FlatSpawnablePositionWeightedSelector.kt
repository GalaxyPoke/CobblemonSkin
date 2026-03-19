/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.selection

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.api.spawning.position.SpawnablePositionType
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.util.removeIf
import com.cobblemon.mod.common.util.weightedSelection
import kotlin.random.Random
import kotlin.reflect.full.createInstance

/**
 * A spawning selector that compiles a distinct list of all spawn details that
 * are possible across the spawnable position list, chooses which spawnable position type
 * to spawn based on a weighted selection of the number of spawns possible in it, performs
 * a weighted selection of spawns in that spawnable position type, and then chooses which
 * of its spawn positions to spawn them on based on their spawnable position-adjusted weights.
 *
 * The goal of this algorithm is to be kinder to spawns that are only possible
 * in very specific locational conditions by not letting the scarcity of suitable
 * locations hurt its chances of spawning. It also tries to allow areas with more or less
 * of an entire spawnable position type to favour whichever are the more populous.
 *
 * The weight for a spawn when doing primary selection is whichever spawnable position-influenced
 * weight is highest (as weight multipliers exist per spawnable position) and then the
 * selection of which spawnable position to spawn the primary selected spawn uses the spawnable
 * position-adjusted weight.
 *
 * At a glance:
 * - Spawn detail selection is flat across spawnable position quantity
 * - Spawnable position type is chosen early by a selection prejudiced in favour of more common spawnable position types.
 *
 * @author Hiroku
 * @since July 10th, 2022
 */
open class FlatSpawnablePositionWeightedSelector : SpawningSelector<FlatSpawnablePositionWeightedSelector.SeparatedSelectionData> {
    open fun getWeight(spawnablePositionType: SpawnablePositionType<*>) = spawnablePositionType.getWeight()

    class SelectingSpawnInformation {
        val spawnablePositionWeights = mutableMapOf<SpawnablePosition, Float>()
        var highestWeight = 0F

        fun add(spawnDetail: SpawnDetail, spawnablePosition: SpawnablePosition, spawnablePositionTypeWeight: Float) {
            val weight = spawnablePosition.getWeight(spawnDetail) * spawnablePositionTypeWeight
            spawnablePositionWeights[spawnablePosition] = weight
            if (weight > highestWeight) {
                highestWeight = weight
            }
        }

        fun chooseSpawnablePosition() = spawnablePositionWeights.keys.toList().weightedSelection { spawnablePositionWeights[it]!! }!!
    }

    class SpawnablePositionSelectionData(
        val spawnToSpawnablePosition: MutableMap<SpawnDetail, SelectingSpawnInformation>,
        var percentSum: Float
    ) {
        val size: Int
            get() = spawnToSpawnablePosition.size

        fun removeSpawnDetails(shouldRemove: (SpawnDetail) -> Boolean) {
            val toRemove = spawnToSpawnablePosition.entries.filter { shouldRemove(it.key) }
            toRemove.forEach { spawnToSpawnablePosition.remove(it.key) }
            percentSum -= toRemove.sumOf { it.key.percentage.toDouble().takeIf { it > 0 } ?: 0.0 }.toFloat()
        }

        fun removeSpawnablePositions(shouldRemove: (SpawnDetail, SpawnablePosition) -> Boolean) {
            val toRemove = spawnToSpawnablePosition.entries.filter { (spawnDetail, positionData) ->
                positionData.spawnablePositionWeights.removeIf { shouldRemove(spawnDetail, it.key) }
                positionData.highestWeight = positionData.spawnablePositionWeights.maxOfOrNull { it.value } ?: 0F
                positionData.spawnablePositionWeights.isEmpty()
            }

            toRemove.forEach { spawnToSpawnablePosition.remove(it.key) }

            percentSum -= toRemove.sumOf { it.key.percentage.toDouble().takeIf { it > 0 } ?: 0.0 }.toFloat()
        }
    }

    class SeparatedSelectionData(
        val spawnablePositionTypeToSpawns: MutableMap<SpawnablePositionType<*>, SpawnablePositionSelectionData>
    ): SpawnSelectionData {
        override val spawnActions = mutableListOf<SpawnAction<*>>()
        override val context = mutableMapOf<String, Any>()

        override fun removeSpawnDetails(shouldRemove: (SpawnDetail) -> Boolean) {
            spawnablePositionTypeToSpawns.values.forEach { it.removeSpawnDetails(shouldRemove) }
            spawnablePositionTypeToSpawns.removeIf { it.value.size == 0 }
        }

        override fun removeSpawnablePositions(shouldRemove: (SpawnDetail, SpawnablePosition) -> Boolean) {
            spawnablePositionTypeToSpawns.values.forEach { it.removeSpawnablePositions(shouldRemove) }
            spawnablePositionTypeToSpawns.removeIf { it.value.size == 0 }
        }
    }

    override fun getSelectionData(
        spawner: Spawner,
        bucket: SpawnBucket,
        spawnablePositions: List<SpawnablePosition>
    ): SeparatedSelectionData {
        val spawnablePositionTypeToSpawns = mutableMapOf<SpawnablePositionType<*>, SpawnablePositionSelectionData>()

        spawnablePositions.forEach { spawnablePosition ->
            val spawnablePositionType = SpawnablePosition.getByClass(spawnablePosition)!!

            val possible = spawner.getMatchingSpawns(bucket, spawnablePosition)
            if (possible.isNotEmpty()) {
                val spawnablePositionSelectionData = spawnablePositionTypeToSpawns.getOrPut(spawnablePositionType) { SpawnablePositionSelectionData(mutableMapOf(), 0F) }
                possible.forEach {
                    // Only add to percentSum if this is the first time we've seen this SpawnDetail for this spawnable
                    // position type, otherwise the percentage will get amplified for every spawnable position the thing
                    // was possible, completely ruining the point of this pre-selection percentage.
                    if (it.percentage > 0 && !spawnablePositionSelectionData.spawnToSpawnablePosition.containsKey(it)) {
                        spawnablePositionSelectionData.percentSum += it.percentage
                    }

                    val selectingSpawnInformation = spawnablePositionSelectionData.spawnToSpawnablePosition.getOrPut(
                        it,
                        SelectingSpawnInformation::class::createInstance
                    )
                    selectingSpawnInformation.add(it, spawnablePosition, getWeight(spawnablePositionType))
                }
            }
        }

        return SeparatedSelectionData(spawnablePositionTypeToSpawns)
    }

    override fun selectSpawnAction(
        spawner: Spawner,
        bucket: SpawnBucket,
        selectionData: SeparatedSelectionData
    ): SpawnAction<*>? {
        if (selectionData.spawnablePositionTypeToSpawns.isEmpty()) {
            return null
        }

        // Which spawnable position type should we use?
        val spawnablePositionSelectionData = selectionData.spawnablePositionTypeToSpawns.entries.toList()
            .weightedSelection { getWeight(it.key) * it.value.size }
            ?.value
            ?: return null

        val spawnToSpawnablePosition = spawnablePositionSelectionData.spawnToSpawnablePosition
        var percentSum = spawnablePositionSelectionData.percentSum

        // First pass is doing percentage checks.
        if (percentSum > 0) {
            if (percentSum > 100) {
                LOGGER.warn(
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

        val selectedSpawn = spawnToSpawnablePosition.entries.toList().weightedSelection { it.value.highestWeight } ?: return null
        return selectedSpawn.key.choose(
            spawnablePosition = selectedSpawn.value.chooseSpawnablePosition(),
            bucket = bucket,
            selectionData = selectionData
        )
    }

    protected fun getProbabilitiesFromSpawnablePositionType(spawner: Spawner, spawnablePositionSelectionData: SpawnablePositionSelectionData): Map<SpawnDetail, Float> {
        val percentSum = spawnablePositionSelectionData.percentSum
        val weightPortion = 100 - percentSum
        val totalWeightMultiplier = 100 / weightPortion
        val spawnToSpawnablePosition = spawnablePositionSelectionData.spawnToSpawnablePosition

        if (percentSum > 100) {
            LOGGER.warn(
                """
                    A spawn list for ${spawner.name} exceeded 100% on percentage sums...
                    This means you don't understand how this option works.
                """.trimIndent()
            )
            return emptyMap()
        }

        val totalWeights = mutableMapOf<SpawnDetail, Float>()
        var totalWeight = 0F

        for (spawn in spawnToSpawnablePosition.values) {
            totalWeight += spawn.highestWeight
        }

        val rescaledTotalWeight = totalWeight * totalWeightMultiplier
        val percentageWeight = (rescaledTotalWeight - totalWeight) / percentSum

        for ((spawnDetail, info) in spawnToSpawnablePosition.entries) {
            totalWeights[spawnDetail] = info.highestWeight + if (spawnDetail.percentage > 0) spawnDetail.percentage * percentageWeight else 0F
        }

        return totalWeights
    }

    override fun getTotalWeights(
        spawner: Spawner,
        bucket: SpawnBucket,
        spawnablePositions: List<SpawnablePosition>
    ): Map<SpawnDetail, Float> {
        val selectionData = getSelectionData(spawner, bucket, spawnablePositions)

        if (selectionData.spawnablePositionTypeToSpawns.isEmpty()) {
            return mapOf()
        }

        val totalWeights = mutableMapOf<SpawnDetail, Float>()

        val totalSpawnablePositionWeight = selectionData.spawnablePositionTypeToSpawns.keys.sumOf { getWeight(it).toDouble() }.toFloat()

        for ((spawnablePositionType, spawnPositionSelectionData) in selectionData.spawnablePositionTypeToSpawns) {
            val spawnPositionWeightCorrection = getWeight(spawnablePositionType) / totalSpawnablePositionWeight
            val spawnPositionProbabilities = getProbabilitiesFromSpawnablePositionType(spawner, spawnPositionSelectionData)

            spawnPositionProbabilities.entries.forEach {
                totalWeights[it.key] = it.value * spawnPositionWeightCorrection
            }
        }

        return totalWeights
    }
}