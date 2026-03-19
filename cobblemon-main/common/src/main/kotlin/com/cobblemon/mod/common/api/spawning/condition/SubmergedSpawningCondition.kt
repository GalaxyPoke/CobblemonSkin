/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.position.SubmergedSpawnablePosition
import com.cobblemon.mod.common.util.Merger
import net.minecraft.world.level.material.Fluid

/**
 * Base type for a spawning condition that applies to some kind of [SubmergedSpawnablePosition]. This
 * can be extended for subclasses of [SubmergedSpawnablePosition].
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
abstract class SubmergedTypeSpawningCondition<T : SubmergedSpawnablePosition> : AreaTypeSpawningCondition<T>() {
    var minDepth: Int? = null
    var maxDepth: Int? = null
    var fluidIsSource: Boolean? = null
    var fluid: RegistryLikeCondition<Fluid>? = null

    override fun fits(spawnablePosition: T): Boolean {
        return if (!super.fits(spawnablePosition)) {
            false
        } else if (minHeight != null && spawnablePosition.height < minHeight!!) {
            return false
        } else if (maxHeight != null && spawnablePosition.height > maxHeight!!) {
            return false
        } else if (minDepth != null && spawnablePosition.depth < minDepth!!) {
            false
        } else if (maxDepth != null && spawnablePosition.depth > maxDepth!!) {
            false
        } else if (fluidIsSource != null && spawnablePosition.fluid.isSource != fluidIsSource!!) {
            false
        } else !(spawnablePosition.fluid.isEmpty || (fluid != null && !fluid!!.fits(spawnablePosition.fluid.type, spawnablePosition.fluidRegistry)))
    }

    override fun copyFrom(other: SpawningCondition<*>, merger: Merger) {
        super.copyFrom(other, merger)
        if (other is SubmergedTypeSpawningCondition) {
            minDepth = merger.mergeSingle(minDepth, other.minDepth)
            maxDepth = merger.mergeSingle(minDepth, other.minDepth)
            fluidIsSource = merger.mergeSingle(fluidIsSource, other.fluidIsSource)
            fluid = merger.mergeSingle(fluid, other.fluid)
        }
    }
}

/**
 * A spawning condition for an [SubmergedSpawnablePosition].
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
open class SubmergedSpawningCondition : SubmergedTypeSpawningCondition<SubmergedSpawnablePosition>() {
    override fun spawnablePositionClass() = SubmergedSpawnablePosition::class.java
    companion object {
        const val NAME = "submerged"
    }
}