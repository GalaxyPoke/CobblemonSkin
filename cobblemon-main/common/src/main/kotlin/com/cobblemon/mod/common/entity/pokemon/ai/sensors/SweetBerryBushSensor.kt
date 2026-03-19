/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.sensors

import com.cobblemon.mod.common.CobblemonMemories
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.Sensor
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CaveVines
import net.minecraft.world.level.block.SweetBerryBushBlock
import net.minecraft.world.level.block.state.BlockState
import java.util.*

class SweetBerryBushSensor : Sensor<LivingEntity>(SCAN_INTERVAL) {

    override fun doTick(level: ServerLevel, entity: LivingEntity) {
        val pos = entity.blockPosition()
        val isDisabled = entity.brain.getMemory(CobblemonMemories.DISABLE_WALK_TO_BERRY_BUSH).orElse(false)
        if (isDisabled) return
        val nearestBush = findNearestBush(level, pos)

        if (!nearestBush.isEmpty) {
            entity.brain.setMemory(CobblemonMemories.NEARBY_SWEET_BERRY_BUSH, nearestBush)
        } else {
            entity.brain.eraseMemory(CobblemonMemories.NEARBY_SWEET_BERRY_BUSH)
        }
    }

    override fun requires(): Set<MemoryModuleType<*>> {
        return setOf(CobblemonMemories.NEARBY_SWEET_BERRY_BUSH)
    }

    private fun findNearestBush(level: ServerLevel, origin: BlockPos): Optional<BlockPos> {
        var closest: BlockPos? = null
        var closestDistSq = Double.MAX_VALUE

        for (pos in BlockPos.betweenClosed(origin.offset(-SCAN_RADIUS_HORIZONTAL, 1 - SCAN_RADIUS_VERTICAL, -SCAN_RADIUS_HORIZONTAL),
                origin.offset(SCAN_RADIUS_HORIZONTAL, SCAN_RADIUS_VERTICAL - 1, SCAN_RADIUS_HORIZONTAL))) {
            val state: BlockState = level.getBlockState(pos)
            if ((state.`is`(Blocks.SWEET_BERRY_BUSH) && state.getValue(SweetBerryBushBlock.AGE) >= 2)
                    || (CaveVines.hasGlowBerries(state))) {
                val distSq = pos.distSqr(origin)
                if (distSq < closestDistSq) {
                    closest = pos.immutable()
                    closestDistSq = distSq
                }
            }
        }

        return Optional.ofNullable(closest)
    }

    companion object {
        const val SCAN_RADIUS_HORIZONTAL = 10
        const val SCAN_RADIUS_VERTICAL = 1
        const val SCAN_INTERVAL = 40
    }
}
