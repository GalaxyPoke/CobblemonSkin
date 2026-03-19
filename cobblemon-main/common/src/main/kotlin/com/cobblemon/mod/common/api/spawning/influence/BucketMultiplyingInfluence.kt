/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.influence

import com.cobblemon.mod.common.api.spawning.SpawnBucket

/**
 * A [SpawningInfluence] that multiplies bucket weights by the provided values
 * and then normalizes the resulting weights so that their total adds up to 100.
 */
class BucketMultiplyingInfluence(
    val multipliers: Map<String, Float>,
) : SpawningInfluence {
    override fun affectBucketWeights(bucketWeights: MutableMap<SpawnBucket, Float>) {
        val updates = mutableMapOf<SpawnBucket, Float>()
        for ((bucket, weight) in bucketWeights) {
            val multiplier = multipliers[bucket.name]
            if (multiplier != null) {
                updates[bucket] = weight * multiplier
            }
        }

        for ((bucket, newWeight) in updates) {
            bucketWeights[bucket] = newWeight
        }

        normalizeBucketWeights(bucketWeights)
    }
}