/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.influence

import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity

class IncenseSweetInfluence(val pos: BlockPos? = null) : SpawningInfluence {

    override fun affectSpawn(action: SpawnAction<*>, entity: Entity) {
    }

    override fun affectWeight(detail: SpawnDetail, spawnablePosition: SpawnablePosition, weight: Float): Float {
        return super.affectWeight(detail, spawnablePosition, weight)
    }
}
