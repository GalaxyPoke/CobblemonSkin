/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

///*
// * Copyright (C) 2023 Cobblemon Contributors
// *
// * This Source Code Form is subject to the terms of the Mozilla Public
// * License, v. 2.0. If a copy of the MPL was not distributed with this
// * file, You can obtain one at https://mozilla.org/MPL/2.0/.
// */
//
//package com.cobblemon.mod.common.api.spawning.influence.detector
//
//import com.cobblemon.mod.common.CobblemonPoiTypes
//import com.cobblemon.mod.common.api.spawning.influence.GrottoImpactedSpawningInfluence
//import com.cobblemon.mod.common.api.spawning.influence.UnconditionalZoneSpawningInfluence
//import com.cobblemon.mod.common.api.spawning.influence.ZoneSpawningInfluence
//import com.cobblemon.mod.common.api.spawning.spawner.Spawner
//import com.cobblemon.mod.common.api.spawning.spawner.SpawningZoneInput
//import com.cobblemon.mod.common.util.toBlockPos
//import net.minecraft.core.BlockPos
//import net.minecraft.core.Holder
//import net.minecraft.server.level.ServerLevel
//import net.minecraft.world.entity.ai.village.poi.PoiManager
//import net.minecraft.world.entity.ai.village.poi.PoiType
//import net.minecraft.world.level.block.state.BlockState
//
//object GrottoBlockDetector : SpawningInfluenceDetector {
//    @JvmField
//    val RANGE: Int = 48
//
//    override fun detectFromInput(spawner: Spawner, input: SpawningZoneInput): List<ZoneSpawningInfluence> {
//        val world = input.world
//        val searchRange = maxOf(RANGE, input.length * 2, input.height * 2)
//        val grottoBlockPositions = world.poiManager.findAll(
//            { holder: Holder<PoiType> -> holder.`is`(CobblemonPoiTypes.GROTTO_BLOCK_KEY) },
//            { true },
//            input.getCenter().toBlockPos(),
//            searchRange,
//            PoiManager.Occupancy.ANY
//        ).toList()
//
//        return if (grottoBlockPositions.isEmpty()) {
//            emptyList()
//        } else {
//            listOf(UnconditionalZoneSpawningInfluence(influence = GrottoImpactedSpawningInfluence()))
//        }
//    }
//
//    override fun detectFromBlock(world: ServerLevel, pos: BlockPos, blockState: BlockState) = emptyList<ZoneSpawningInfluence>()
//}