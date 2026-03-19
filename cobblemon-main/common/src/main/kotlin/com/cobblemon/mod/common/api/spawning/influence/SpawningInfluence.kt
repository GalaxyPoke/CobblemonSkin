/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.influence

import com.cobblemon.mod.common.api.spawning.BestSpawner
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

/**
 * An influence over spawning that can affect various parts of the spawning process. These can be attached to
 * various areas of the [BestSpawner] for either momentary or extended effects.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
interface SpawningInfluence {
    /** Whether this influence has passed and should be removed. */
    fun isExpired(): Boolean = false
    /** Runs for each influence when added to the spawnable position. */
    fun affectSpawnablePosition(spawnablePosition: SpawnablePosition) {}
    /** Returns true if the given spawn detail is able to spawn under this influence. */
    fun affectSpawnable(detail: SpawnDetail, spawnablePosition: SpawnablePosition): Boolean = true
    /** Returns the effective weight of spawning under this influence. This is after typical weight multipliers. */
    fun affectWeight(detail: SpawnDetail, spawnablePosition: SpawnablePosition, weight: Float): Float = weight
    /** Affects the spawn action prior to it generating the entity. */
    fun affectAction(action: SpawnAction<*>) {}
    /** Applies some influence over the entity that's been spawned. */
    fun affectSpawn(action: SpawnAction<*>, entity: Entity) {}
    /** Applies some influence over the weight of spawn buckets. */
    fun affectBucketWeights(bucketWeights: MutableMap<SpawnBucket, Float>) {}
    /** Normalizes all bucket weights to add up to 100 */
    fun normalizeBucketWeights(bucketWeights: MutableMap<SpawnBucket, Float>) {
        val sum = bucketWeights.values.sum()
        val to100 = 100F / sum
        bucketWeights.keys.toList().forEach {
            val weight = bucketWeights[it] ?: return@forEach
            bucketWeights[it] = weight * to100
        }
    }
    /** Filters positions that could be usable for a spawnable position calculator */
    fun isAllowedPosition(world: ServerLevel, pos: BlockPos, spawnablePositionCalculator: SpawnablePositionCalculator<*, *>): Boolean = true
    /**
     * Injects additional spawn possibilities for the given bucket and spawnable position.
     *
     * You should make sure the spawn position satisfies any spawn details that you inject.
     */
    fun injectSpawns(bucket: SpawnBucket, spawnablePosition: SpawnablePosition): List<SpawnDetail>? = null
}