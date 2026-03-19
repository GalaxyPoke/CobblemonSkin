/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.influence

import com.cobblemon.mod.common.api.spawning.SpawnBucket
import kotlin.math.pow

/**
 * A [SpawningInfluence] that normalizes the weights of buckets based on a tier system. The idea is that
 * normally the rarest buckets are MUCH rarer than the most common buckets, but in some cases we want
 * those distances to be closer to each other. This is usually for when we're doing fewer spawn actions
 * overall, meaning that the rarest buckets become very harsh, or we want to apply lure bonuses.
 *
 * The [tier] is an integer stepping up through different degrees of normalization. This is used with [gradient]
 * and [firstTier] to control the rate at which the tiers move the buckets. The equation for the new
 * weight of a bucket is `weight ^ (1 / normalizationFactor)`. This means that a normalization factor of 1
 * will not change the weights at all, and a normalization factor of 2 will make each weight turn into its
 * square root, and so on. As this number grows, the closer the bucket weights will be to each other.
 *
 * The normalizationFactor is calculated as `initial + (gradient * tier)`.
 *
 * @author Hiroku
 * @since April 20th, 2025
 */
open class BucketNormalizingInfluence(
    val tier: Int = 0,
    val gradient: Float = 0.2F,
    val firstTier: Float = 1.29F
) : SpawningInfluence {
    override fun affectBucketWeights(bucketWeights: MutableMap<SpawnBucket, Float>) {
        if (tier == 0) {
            return
        }

        val normalizationFactor = firstTier + (gradient * (tier - 1)).toDouble()
        bucketWeights.keys.toList().forEach {
            val weight = bucketWeights[it] ?: return@forEach
            bucketWeights[it] = weight.toDouble().pow(1 / normalizationFactor).toFloat()
        }

        normalizeBucketWeights(bucketWeights)
    }
}