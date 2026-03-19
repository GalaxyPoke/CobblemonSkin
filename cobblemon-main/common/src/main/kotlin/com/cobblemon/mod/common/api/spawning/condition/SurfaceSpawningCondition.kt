/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.position.SurfaceSpawnablePosition
import com.cobblemon.mod.common.util.Merger
import net.minecraft.world.level.material.Fluid

/**
 * Base type for a spawning condition that applies to some kind of [SurfaceSpawnablePosition]. This
 * can be extended for subclasses of [SurfaceSpawnablePosition].
 *
 * @author Hiroku
 * @since December 15th, 2022
 */
abstract class SurfaceTypeSpawningCondition<T : SurfaceSpawnablePosition> : AreaTypeSpawningCondition<T>() {
    var minDepth: Int? = null
    var maxDepth: Int? = null
    var fluid: RegistryLikeCondition<Fluid>? = null

    override fun fits(spawnablePosition: T): Boolean {
        return if (!super.fits(spawnablePosition)) {
            false
        } else if (minDepth != null && spawnablePosition.depth < minDepth!!) {
            false
        } else if (maxDepth != null && spawnablePosition.depth > maxDepth!!) {
            false
        } else !(spawnablePosition.baseBlock.fluidState.isEmpty || (fluid != null && !fluid!!.fits(spawnablePosition.baseBlock.fluidState.type, spawnablePosition.fluidRegistry)))
    }

    override fun copyFrom(other: SpawningCondition<*>, merger: Merger) {
        super.copyFrom(other, merger)
        if (other is SurfaceTypeSpawningCondition) {
            minDepth = merger.mergeSingle(minDepth, other.minDepth)
            maxDepth = merger.mergeSingle(minDepth, other.minDepth)
            fluid = merger.mergeSingle(fluid, other.fluid)
        }
    }
}

/**
 * A spawning condition for a [SurfaceSpawnablePosition].
 *
 * @author Hiroku
 * @since December 15th, 2022
 */
open class SurfaceSpawningCondition : SurfaceTypeSpawningCondition<SurfaceSpawnablePosition>() {
    override fun spawnablePositionClass() = SurfaceSpawnablePosition::class.java
    companion object {
        const val NAME = "surface"
    }
}