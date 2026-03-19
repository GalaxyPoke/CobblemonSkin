/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.sensors

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.block.SaccharineLeafBlock
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.distanceTo
import com.cobblemon.mod.common.util.getMemorySafely
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.sensing.Sensor
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import kotlin.time.measureTime

class SacLeavesSensor : Sensor<PokemonEntity>(120) {

    override fun requires() = setOf(CobblemonMemories.NEARBY_SACC_LEAVES)

    override fun doTick(world: ServerLevel, entity: PokemonEntity) {
        val brain = entity.brain

        val searchRadius = 7
        val tooFarDistance = 30
        val wayTooFarDistance = 64
        val currPos: BlockPos? = brain.getMemorySafely(CobblemonMemories.NEARBY_SACC_LEAVES).orElse(null)

        if (currPos != null && isValidLeafBlock(entity.level().getBlockState(currPos))) {
            val distance = entity.distanceTo(currPos)
            if (distance <= tooFarDistance) {
                return
            } else if (distance > wayTooFarDistance) {
                brain.eraseMemory(CobblemonMemories.NEARBY_SACC_LEAVES)
            }
        }

        val centerPos = entity.blockPosition()
        var leavesPos: BlockPos? = null
        var shortestDist = Double.MAX_VALUE
        BlockPos.betweenClosedStream(
            centerPos.offset(-searchRadius, -2, -searchRadius),
            centerPos.offset(searchRadius, searchRadius, searchRadius)
        ).forEach { pos ->
            val state = world.getBlockState(pos)
            if (isValidLeafBlock(state)) {
                val distance = pos.distSqr(Vec3i(centerPos.x, centerPos.y, centerPos.z))
                if (distance < shortestDist) {
                    leavesPos = BlockPos(pos)
                    shortestDist = distance
                }
            }
        }

        if (leavesPos != null) {
            brain.setMemory(CobblemonMemories.NEARBY_SACC_LEAVES, leavesPos)
        } else {
            brain.eraseMemory(CobblemonMemories.NEARBY_SACC_LEAVES)
        }
    }

    private fun isValidLeafBlock(state: BlockState): Boolean {
        if (state.block !is SaccharineLeafBlock) return false
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(
                BlockStateProperties.WATERLOGGED
            ) as Boolean
        ) {
            return false
        }
        return state.getValue(SaccharineLeafBlock.AGE) != SaccharineLeafBlock.MAX_AGE
    }
}
