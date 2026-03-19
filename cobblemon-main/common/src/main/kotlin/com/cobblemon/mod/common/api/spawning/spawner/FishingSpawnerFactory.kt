/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.spawner

import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnRules
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.api.spawning.influence.BucketMultiplyingInfluence
import com.cobblemon.mod.common.api.spawning.influence.BucketNormalizingInfluence
import com.cobblemon.mod.common.api.spawning.influence.PlayerLevelRangeInfluence
import com.cobblemon.mod.common.api.spawning.influence.PlayerLevelRangeInfluence.Companion.TYPICAL_VARIATION
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.util.mutableLazy
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import com.cobblemon.mod.common.api.spawning.BestSpawner;
import com.cobblemon.mod.common.api.spawning.position.FishingSpawnablePosition


/**
 * This isn't really a factory, one FishingSpawner is created as a field on [BestSpawner] on
 * server start.  However, this emulates the systems that PlayerSpawner uses so extenders can
 * add influences that affect all fishing encounters.
 *
 * Responsible for creating [BasicSpawner]s for Fishing with whatever appropriate settings.
 * You can swap over the spawn pool and the influences here, and it will apply to all newly-generated
 * [BasicSpawner]s for Fishing.  Has a global every-cast influence list, as well as a context-sensitive
 * influence list.
 *
 * @author Andy
 * @since January 23rd, 2026
 */
object FishingSpawnerFactory {
    var spawnPool: SpawnPool by mutableLazy {
        CobblemonSpawnPools.WORLD_SPAWN_POOL
    }

    data class Context(
        val player: ServerPlayer,
        val rodStack: ItemStack,
        val lureLevel: Int,
        val luckOfTheSeaLevel: Int,
        val world: ServerLevel,
        val pos: BlockPos,
        val spawner: BasicSpawner
    )

    /**
     * List of influences for a new cast of the fishing rod. Context is provided at
     * build time in [buildPositionInfluences]
     */
    var positionInfluenceBuilders = mutableListOf<(ctx: Context) -> SpawningInfluence?>(
        { ctx -> PlayerLevelRangeInfluence(ctx.player, TYPICAL_VARIATION) },
        { ctx -> BucketNormalizingInfluence(tier = ctx.lureLevel + ctx.luckOfTheSeaLevel) }
    )

    fun createSharedSpawner(): BasicSpawner {
        return BasicSpawner(
            name = "fishing",
            spawnPool = spawnPool
        ).also { spawner ->
            spawner.influences += BucketMultiplyingInfluence(
                multipliers = mapOf(
                    "uncommon" to 2.25f,
                    "rare" to 5.5f,
                    "ultra-rare" to 5.5f,
                )
            )
        }
    }

    /**
     * Builds the per-cast influences for [FishingSpawnablePosition]
     */
    fun buildPositionInfluences(ctx: Context): MutableList<SpawningInfluence> {
        return positionInfluenceBuilders.mapNotNullTo(mutableListOf()) { it(ctx) }
    }
}
