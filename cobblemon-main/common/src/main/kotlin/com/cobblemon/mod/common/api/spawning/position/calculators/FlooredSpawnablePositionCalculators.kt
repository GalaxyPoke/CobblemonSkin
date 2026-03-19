/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.position.calculators

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.spawning.position.*
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator.Companion.isAirCondition
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator.Companion.isLavaCondition
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator.Companion.isSolidCondition
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator.Companion.isWaterCondition
import net.minecraft.world.level.block.state.BlockState

/**
 * A spawnable position calculator that creates some kind of [FlooredSpawnablePosition]. The shared
 * idea of these spawnable positions is that there is a base block condition for the floor, and then some
 * other condition for its surroundings.
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
interface FlooredSpawnablePositionCalculator<T : FlooredSpawnablePosition> : AreaSpawnablePositionCalculator<T> {
    /** The condition that must be met by the base block. */
    val baseCondition: (BlockState) -> Boolean
    /** The condition that must be met by the surrounding blocks. */
    val surroundingCondition: (BlockState) -> Boolean

    override fun fits(input: AreaSpawningInput): Boolean {
        val floorState = input.zone.getBlockState(input.position)
        val aboveState = input.zone.getBlockState(input.position.above())
        return baseCondition(floorState) && surroundingCondition(aboveState)
    }
}

/**
 * The spawnable position calculator used for [GroundedSpawnablePosition]s. Requires a solid block below it and
 * air blocks in its surroundings.
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
object GroundedSpawnablePositionCalculator : FlooredSpawnablePositionCalculator<GroundedSpawnablePosition> {
    override val name = "grounded"
    override val baseCondition: (BlockState) -> Boolean = isSolidCondition
    override val surroundingCondition: (BlockState) -> Boolean = isAirCondition

    override fun calculate(input: AreaSpawningInput): GroundedSpawnablePosition {
        return GroundedSpawnablePosition(
            cause = input.cause,
            world = input.world,
            position = input.position.immutable(),
            light = getLight(input),
            skyLight = getSkyLight(input),
            canSeeSky = getCanSeeSky(input),
            influences = input.spawner.copyInfluences(),
            height = getHeight(input, surroundingCondition, config.maxVerticalSpace, offsetY = 1),
            zone = input.zone,
            nearbyBlocks = getNearbyBlocks(input)
        )
    }
}

/**
 * The spawnable position calculator used for [SeafloorSpawnablePosition]s. Requires a solid block below it and
 * water blocks in its surroundings.
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
object SeafloorSpawnablePositionCalculator : FlooredSpawnablePositionCalculator<SeafloorSpawnablePosition> {
    override val name = "seafloor"
    override val baseCondition: (BlockState) -> Boolean = isSolidCondition
    override val surroundingCondition: (BlockState) -> Boolean = isWaterCondition

    override fun calculate(input: AreaSpawningInput): SeafloorSpawnablePosition {
        return SeafloorSpawnablePosition(
            cause = input.cause,
            world = input.world,
            position = input.position.immutable(),
            light = getLight(input),
            skyLight = getSkyLight(input),
            canSeeSky = getCanSeeSky(input),
            influences = input.spawner.copyInfluences(),
            height = getHeight(input, surroundingCondition, config.maxVerticalSpace, offsetY = 1),
            zone = input.zone,
            nearbyBlocks = getNearbyBlocks(input)
        )
    }
}

/**
 * The spawnable position calculator used for [LavafloorSpawnablePosition]s. Requires a solid block below it and
 * lava blocks in its surroundings.
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
object LavafloorSpawnablePositionCalculator : FlooredSpawnablePositionCalculator<LavafloorSpawnablePosition> {
    override val name = "lavafloor"
    override val baseCondition: (BlockState) -> Boolean = isSolidCondition
    override val surroundingCondition: (BlockState) -> Boolean = isLavaCondition

    override fun calculate(input: AreaSpawningInput): LavafloorSpawnablePosition {
        return LavafloorSpawnablePosition(
            cause = input.cause,
            world = input.world,
            position = input.position.immutable(),
            light = getLight(input),
            skyLight = getSkyLight(input),
            canSeeSky = getCanSeeSky(input),
            influences = input.spawner.copyInfluences(),
            height = getHeight(input, surroundingCondition, config.maxVerticalSpace, offsetY = 1),
            zone = input.zone,
            nearbyBlocks = getNearbyBlocks(input)
        )
    }
}


/**
 * The spawnable position calculator used for [SurfaceSpawnablePosition]s. Requires a fluid block below it and
 * air blocks in its surroundings.
 *
 * @author Hiroku
 * @since December 15th, 2022
 */
object SurfaceSpawnablePositionCalculator : FlooredSpawnablePositionCalculator<SurfaceSpawnablePosition> {
    override val name = "surface"
    override val baseCondition: (BlockState) -> Boolean = { !it.fluidState.isEmpty }
    override val surroundingCondition: (BlockState) -> Boolean = isAirCondition

    override fun calculate(input: AreaSpawningInput): SurfaceSpawnablePosition {
        return SurfaceSpawnablePosition(
            cause = input.cause,
            world = input.world,
            position = input.position.immutable(),
            light = getLight(input),
            skyLight = getSkyLight(input),
            canSeeSky = getCanSeeSky(input),
            influences = input.spawner.copyInfluences(),
            height = getHeight(input, surroundingCondition, config.maxVerticalSpace / 2, offsetY = 1),
            depth = getDepth(input, baseCondition, config.maxVerticalSpace / 2),
            zone = input.zone,
            nearbyBlocks = getNearbyBlocks(input)
        )
    }
}

