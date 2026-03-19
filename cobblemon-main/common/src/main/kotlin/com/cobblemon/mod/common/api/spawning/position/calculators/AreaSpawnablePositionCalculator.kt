/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.position.calculators

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.spawning.SpawningZone
import com.cobblemon.mod.common.api.spawning.position.AreaSpawnablePositionResolver
import com.cobblemon.mod.common.api.spawning.position.AreaSpawnablePosition
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

/**
 * A spawnable position calculator that deals with [AreaSpawnablePosition]s. These work off [AreaSpawningInput]
 * instances, and must output some kind of [AreaSpawnablePosition].
 *
 * This is the interface to use for all the spawnable positions that will get used by world spawning as that
 * is the primary case that area spawning is done. These get registered when creating the spawnable position type, in
 * [SpawnablePosition.register].
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
interface AreaSpawnablePositionCalculator<O : AreaSpawnablePosition> : SpawnablePositionCalculator<AreaSpawningInput, O> {
    /**
     * Whether this spawnable position calculator is likely to provide a value for this location.
     *
     * This should be relatively final. See how the [AreaSpawnablePositionResolver] works to understand why
     * you should be pretty sure before returning true to this function.
     */
    fun fits(input: AreaSpawningInput): Boolean

    fun getDepth(input: AreaSpawningInput, condition: (BlockState) -> Boolean, maximum: Int): Int
        = input.zone.depthSpace(input.position.x, input.position.y, input.position.z, condition, maximum)
    fun getHeight(input: AreaSpawningInput, condition: (BlockState) -> Boolean, maximum: Int, offsetX: Int = 0, offsetY: Int = 0, offsetZ: Int = 0): Int
        = input.zone.heightSpace(input.position.x + offsetX, input.position.y + offsetY, input.position.z + offsetZ, condition, maximum)
    fun getHorizontalSpace(input: AreaSpawningInput, condition: (BlockState) -> Boolean, maximum: Int, offsetX: Int = 0, offsetY: Int = 0, offsetZ: Int = 0): Int
        = input.zone.horizontalSpace(input.position.x + offsetX, input.position.y + offsetY, input.position.z + offsetZ, condition, maximum)
    fun getLight(input: AreaSpawningInput, elseLight: Int = 0): Int
        = input.zone.getLight(input.position.x, input.position.y + 1, input.position.z, elseLight)
    fun getSkyLight(input: AreaSpawningInput, elseLight: Int = 0): Int
        = input.zone.getSkyLight(input.position.x, input.position.y + 1, input.position.z, elseLight)
    fun getCanSeeSky(input: AreaSpawningInput): Boolean = input.zone.canSeeSky(input.position.x, input.position.y + 1, input.position.z)
    fun getSkySpaceAbove(input: AreaSpawningInput): Int = input.zone.skySpaceAbove(input.position.x, input.position.y, input.position.z)
    fun getNearbyBlocks(
        input: AreaSpawningInput,
        horizontalRadius: Int = config.maxNearbyBlocksHorizontalRange,
        verticalRadius: Int = config.maxNearbyBlocksVerticalRange
    ) = input.zone.nearbyBlocks(input.position, horizontalRadius, verticalRadius)

}

open class AreaSpawningInput(val spawner: Spawner, var position: BlockPos, val zone: SpawningZone) : SpawnablePositionInput(zone.cause, zone.world)