/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.selection

import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.api.spawning.spawner.Spawner


/**
 * Interface responsible for taking all the potential spawns across many positions, and applying some kind of
 * selection process to make some [SpawnAction]s. It is also responsible for generating a name to percentage
 * probability for the given spawn information for checking possible spawns under specific conditions.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
interface SpawningSelector<T : SpawnSelectionData> {
    companion object {
        @JvmField
        val DEFAULT = FlatSpawnablePositionWeightedSelector()
    }

    fun getSelectionData(spawner: Spawner, bucket: SpawnBucket, spawnablePositions: List<SpawnablePosition>): T

    fun selectSpawnAction(
        spawner: Spawner,
        bucket: SpawnBucket,
        selectionData: T
    ): SpawnAction<*>?

    fun select(spawner: Spawner, bucket: SpawnBucket, spawnablePositions: List<SpawnablePosition>, maxSpawns: Int): List<SpawnAction<*>> {
        val selectionData = getSelectionData(spawner, bucket, spawnablePositions)

        val spawnActions = selectionData.spawnActions

        while (spawnActions.size < maxSpawns) {
            val spawnAction = selectSpawnAction(
                spawner = spawner,
                bucket = bucket,
                selectionData = selectionData
            )

            if (spawnAction == null) {
                break
            }

            spawnActions.add(spawnAction)
        }

        return spawnActions
    }

    fun getProbabilities(spawner: Spawner, bucket: SpawnBucket, spawnablePositions: List<SpawnablePosition>): Map<SpawnDetail, Float> {
        val weights = getTotalWeights(spawner, bucket, spawnablePositions)
        val totalWeight = weights.values.sum()
        val percentages = mutableMapOf<SpawnDetail, Float>()
        weights.forEach { (spawnDetail, weight) -> percentages[spawnDetail] = (weight / totalWeight * 100F).coerceIn(0F..100F) }
        return percentages
    }

    fun getTotalWeights(spawner: Spawner, bucket: SpawnBucket, spawnablePositions: List<SpawnablePosition>): Map<SpawnDetail, Float>
}