/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.position.calculators

import com.cobblemon.mod.common.api.PrioritizedList
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.tags.FluidTags
import net.minecraft.world.level.block.state.BlockState

/**
 * Calculates some kind of [SpawnablePosition] from a particular type of input data. This
 * is necessary when you create a new type of [SpawnablePosition] with [SpawnablePosition.register]
 * since it informs the spawner how to actually create spawnable positions of that type.
 *
 * If you are adding to the world spawner, then you probably actually want to create an
 * [AreaSpawnablePositionCalculator].
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
interface SpawnablePositionCalculator<I : SpawnablePositionInput, O : SpawnablePosition> {
    companion object {
        val isAirCondition: (BlockState) -> Boolean = { it.isAir || (!it.isSolid && !it.fluidState.`is`(FluidTags.WATER) && !it.`is`(BlockTags.RAILS)) }
        val isSolidCondition: (BlockState) -> Boolean = { it.isSolid }
        val isWaterCondition: (BlockState) -> Boolean = { it.fluidState.`is`(FluidTags.WATER) && it.fluidState.isSource  }
        val isLavaCondition: (BlockState) -> Boolean = { it.fluidState.`is`(FluidTags.LAVA) && it.fluidState.isSource }

        private val calculators = PrioritizedList<SpawnablePositionCalculator<*, *>>()

        val prioritizedAreaCalculators: List<AreaSpawnablePositionCalculator<*>>
            get() = calculators.filterIsInstance<AreaSpawnablePositionCalculator<*>>()

        fun register(calculator: SpawnablePositionCalculator<*, *>, priority: Priority = Priority.NORMAL) {
            calculators.add(priority, calculator)
        }

        fun unregister(calculator: SpawnablePositionCalculator<*, *>) {
            calculators.remove(calculator)
        }
    }

    val name: String

    /** Tries creating a [SpawnablePosition] from the given input. Returning null should be a last resort. */
    fun calculate(input: I): O?
}

/**
 * Base class for input to a [SpawnablePositionCalculator].
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
open class SpawnablePositionInput(
    /** What caused the spawn spawnable position, as a [SpawnCause]. */
    val cause: SpawnCause,
    /** The [ServerLevel] the spawnable position exists in. */
    val world: ServerLevel
)
