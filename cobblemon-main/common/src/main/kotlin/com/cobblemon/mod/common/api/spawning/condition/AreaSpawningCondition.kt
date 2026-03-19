/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.position.AreaSpawnablePosition
import com.cobblemon.mod.common.util.Merger
import net.minecraft.world.level.block.Block

/**
 * Base type for a spawning condition that applies to some kind of [AreaSpawnablePosition]. This
 * can be extended for subclasses of [AreaSpawnablePosition].
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
abstract class AreaTypeSpawningCondition<T : AreaSpawnablePosition> : SpawningCondition<T>() {
    var minHeight: Int? = null
    var maxHeight: Int? = null
    var neededNearbyBlocks: MutableList<RegistryLikeCondition<Block>>? = null

    override fun fits(spawnablePosition: T): Boolean {
        if (!super.fits(spawnablePosition)) {
            return false
        } else if (minHeight != null && spawnablePosition.height < minHeight!!) {
            return false
        } else if (maxHeight != null && spawnablePosition.height > maxHeight!!) {
            return false
        } else if (neededNearbyBlocks != null && neededNearbyBlocks!!.none { cond -> spawnablePosition.nearbyBlockHolders.any { cond.fits(it) } }) {
            return false
        } else {
            return true
        }
    }

    override fun copyFrom(other: SpawningCondition<*>, merger: Merger) {
        super.copyFrom(other, merger)
        if (other is AreaTypeSpawningCondition) {
            merger.mergeSingle(minHeight, other.minHeight)
            merger.mergeSingle(maxHeight, other.maxHeight)
            neededNearbyBlocks = merger.merge(neededNearbyBlocks, other.neededNearbyBlocks)?.toMutableList()
        }
    }

    override fun isValid(): Boolean {
        val containsNullValues = neededNearbyBlocks != null && neededNearbyBlocks!!.any { it == null }
        return super.isValid() && !containsNullValues
    }
}

/**
 * A spawning condition for an [AreaSpawnablePosition].
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
class AreaSpawningCondition : AreaTypeSpawningCondition<AreaSpawnablePosition>() {
    override fun spawnablePositionClass(): Class<out AreaSpawnablePosition> = AreaSpawnablePosition::class.java
    companion object {
        const val NAME = "area"
    }
}