/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.multiplier

import com.cobblemon.mod.common.api.spawning.condition.SpawningCondition
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence

/**
 * A weight multiplying [SpawningInfluence] that applies only under specific conditions. The
 * conditions or anticonditions for this multiplier to apply are the [conditions] and
 * [anticonditions] respectively.
 *
 * For the multiplier to apply, both of the following statements must be true:
 * 1: The conditions list either is empty or at least one of the conditions are met.
 * 2: The anticonditions list either is empty or none of the anticonditions are met.
 *
 * @author Hiroku
 * @since July 8th, 2022
 */
class WeightMultiplier : SpawningInfluence {
    var conditions = mutableListOf<SpawningCondition<*>>()
    var anticonditions = mutableListOf<SpawningCondition<*>>()
    var multiplier = 1F

    override fun affectWeight(detail: SpawnDetail, spawnablePosition: SpawnablePosition, weight: Float): Float {
        val meetsConditions = (conditions.isEmpty() || conditions.any { it.isSatisfiedBy(spawnablePosition) })
                && (anticonditions.isEmpty() || anticonditions.none { it.isSatisfiedBy(spawnablePosition) })
        return if (meetsConditions) multiplier * weight else weight
    }
}