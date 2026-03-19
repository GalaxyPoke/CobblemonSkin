/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.influence.detector

import com.cobblemon.mod.common.api.spawning.SpawningZone
import com.cobblemon.mod.common.api.spawning.SpawningZoneGenerator
import com.cobblemon.mod.common.api.spawning.influence.SpawningZoneInfluence
import com.cobblemon.mod.common.api.spawning.prospecting.IncenseSweetDetector
import com.cobblemon.mod.common.api.spawning.prospecting.SaccharineLogSlatheredDetector
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.api.spawning.spawner.SpawningZoneInput
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState

/**
 * Prospects for [SpawningZoneInfluence] in a world at a given position. Occurs as part of a
 * [SpawningZoneGenerator] scanning a part of the world while formulating a [SpawningZone].
 *
 * @author Hiroku
 * @since March 9th, 2025
 */
interface SpawningInfluenceDetector {
    companion object {
        @JvmStatic
        val detectors = mutableSetOf<SpawningInfluenceDetector>(
            SaccharineLogSlatheredDetector,
            IncenseSweetDetector
        )
    }

    fun detectFromInput(spawner: Spawner, input: SpawningZoneInput) : List<SpawningZoneInfluence>
    fun detectFromBlock(world: ServerLevel, pos: BlockPos, blockState: BlockState): List<SpawningZoneInfluence>
}