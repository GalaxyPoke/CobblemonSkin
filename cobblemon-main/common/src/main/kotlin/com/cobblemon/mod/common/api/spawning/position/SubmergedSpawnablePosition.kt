/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.position

import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.SpawningZone
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos

/**
 * A type of area based spawnable position with a fluid base block.
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
open class SubmergedSpawnablePosition(
    cause: SpawnCause,
    world: ServerLevel,
    position: BlockPos,
    light: Int,
    skyLight: Int,
    canSeeSky: Boolean,
    influences: MutableList<SpawningInfluence>,
    height: Int,
    val depth: Int,
    nearbyBlocks: List<BlockState>,
    zone: SpawningZone
) : AreaSpawnablePosition(cause, world, position, light, skyLight, canSeeSky, influences, height, nearbyBlocks, zone) {
    val fluid = zone.getBlockState(position.x, position.y, position.z).fluidState

    override fun isSafeSpace(world: ServerLevel, pos: BlockPos, state: BlockState) = state.fluidState.type == fluid.type
}