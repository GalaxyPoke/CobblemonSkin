/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.prospecting

import com.cobblemon.mod.common.CobblemonPoiTypes
import com.cobblemon.mod.common.api.spawning.influence.IncenseSweetInfluence
import com.cobblemon.mod.common.api.spawning.influence.SpatialSpawningZoneInfluence
import com.cobblemon.mod.common.api.spawning.influence.SpawningZoneInfluence
import com.cobblemon.mod.common.api.spawning.influence.detector.SpawningInfluenceDetector
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.api.spawning.spawner.SpawningZoneInput
import com.cobblemon.mod.common.util.math.pow
import com.cobblemon.mod.common.util.toBlockPos
import kotlin.math.ceil
import kotlin.math.sqrt
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.village.poi.PoiManager
import net.minecraft.world.entity.ai.village.poi.PoiType
import net.minecraft.world.level.block.state.BlockState

object IncenseSweetDetector : SpawningInfluenceDetector {
    @JvmField
    val RANGE: Int = 32

    override fun detectFromInput(spawner: Spawner, input: SpawningZoneInput): MutableList<SpawningZoneInfluence> {
        val world = input.world
        val listOfInfluences = mutableListOf<SpawningZoneInfluence>()

        val searchRange = RANGE + ceil(sqrt(((input.length pow 2) + (input.width pow 2)).toDouble())).toInt()
        val centerPos = input.getCenter().toBlockPos()

        // TODO after 1.7
//        val sweetIncensePositions = world.poiManager.findAll(
//                { holder: Holder<PoiType> -> holder.`is`(CobblemonPoiTypes.INCENSE_SWEET_KEY) },
//                { true },
//                centerPos,
//                searchRange,
//                PoiManager.Occupancy.ANY
//        ).toList()
//
//        for (pos in sweetIncensePositions) {
//            val influence = SpatialSpawningZoneInfluence(pos, radius = RANGE.toFloat(), IncenseSweetInfluence(pos))
//            listOfInfluences.add(influence)
//        }

        return listOfInfluences
    }

    override fun detectFromBlock(
        world: ServerLevel,
        pos: BlockPos,
        blockState: BlockState
    ): List<SpawningZoneInfluence> = emptyList()
}
