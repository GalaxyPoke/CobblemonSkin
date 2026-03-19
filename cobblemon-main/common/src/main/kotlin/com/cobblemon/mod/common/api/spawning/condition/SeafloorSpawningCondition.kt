/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.position.SeafloorSpawnablePosition
import com.cobblemon.mod.common.util.Merger
import net.minecraft.world.level.block.Block

/**
 * Base type for a spawning condition that applies to some kind of [SeafloorSpawnablePosition]. This
 * can be extended for subclasses of [SeafloorSpawnablePosition].
 *
 * Borrowed the Grounded spawnable position code since it was unintentionally spawning Pokémon on the sea floor
 * already. Seems to be working fine /shrug
 *
 * @author FrankTheFarmer
 * @since May 22nd, 2024
 */
abstract class SeafloorTypeSpawningCondition<T : SeafloorSpawnablePosition> : AreaTypeSpawningCondition<T>() {
    var neededBaseBlocks: MutableList<RegistryLikeCondition<Block>>? = null

    override fun fits(spawnablePosition: T): Boolean {
        return if (!super.fits(spawnablePosition)) {
            false
        } else if (minHeight != null && spawnablePosition.height < minHeight!!) {
            false
        } else if (maxHeight != null && spawnablePosition.height > maxHeight!!) {
            false
        } else if (neededBaseBlocks != null && neededBaseBlocks!!.none { it.fits(spawnablePosition.baseBlockHolder) }) {
            false
        } else {
            true
        }
    }

    override fun copyFrom(other: SpawningCondition<*>, merger: Merger) {
        super.copyFrom(other, merger)
        if (other is SeafloorTypeSpawningCondition) {
            neededBaseBlocks = merger.merge(neededBaseBlocks, other.neededBaseBlocks)?.toMutableList()
        }
    }

    override fun isValid(): Boolean {
        val containsNullValues = neededBaseBlocks != null && neededBaseBlocks!!.any { it == null }
        return super.isValid() && !containsNullValues
    }
}

/**
 * A spawning condition for a [SeafloorSpawnablePosition].
 *
 * @author FrankTheFarmer
 * @since May 22nd, 2024
 */
open class SeafloorSpawningCondition : SeafloorTypeSpawningCondition<SeafloorSpawnablePosition>() {
    override fun spawnablePositionClass() = SeafloorSpawnablePosition::class.java
    companion object {
        const val NAME = "seafloor"
    }
}