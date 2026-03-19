/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.ObserverBlock

fun Level.activateNearbyObservers(blockPos: BlockPos) {
    val currentState = getBlockState(blockPos)
    sendBlockUpdated(blockPos, currentState, currentState, 3)

    for (direction in Direction.entries) {
        val neighborPos = blockPos.relative(direction)
        val neighborState = getBlockState(neighborPos)

        if (neighborState.block == Blocks.OBSERVER) {
            val facing = neighborState.getValue(ObserverBlock.FACING)
            if (neighborPos.relative(facing) == blockPos) {
                val updatedState = neighborState.setValue(ObserverBlock.POWERED, true)
                setBlock(neighborPos, updatedState, 3)
                scheduleTick(neighborPos, Blocks.OBSERVER, 2)
            }
        }
    }
}
