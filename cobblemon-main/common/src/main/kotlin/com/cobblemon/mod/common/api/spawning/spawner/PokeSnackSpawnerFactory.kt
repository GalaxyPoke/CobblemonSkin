/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.spawner

import com.cobblemon.mod.common.api.fishing.SpawnBait
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnRules
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.api.spawning.influence.BucketMultiplyingInfluence
import com.cobblemon.mod.common.api.spawning.influence.BucketNormalizingInfluence
import com.cobblemon.mod.common.api.spawning.influence.SpawnBaitInfluence
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.rules.SpawnRule
import com.cobblemon.mod.common.util.mutableLazy
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel

/**
 * Responsible for creating [FixedAreaSpawner]s for PokeSnacks with whatever appropriate settings.
 * You can swap over the spawn pool and the influences here, and it will apply to all newly-generated
 * [FixedAreaSpawner]s for PokeSnacks.
 *
 * @author Andy
 * @since January 23rd, 2026
 */
object PokeSnackSpawnerFactory {
    var spawnPool: SpawnPool by mutableLazy {
        CobblemonSpawnPools.WORLD_SPAWN_POOL
    }

    data class Context(
        val world: ServerLevel,
        val pos: BlockPos,
        val horizontalRadius: Int,
        val verticalRadius: Int,
        val maxPokemonPerChunk: Float,
        val name: String,
        val baitEffects: List<SpawnBait.Effect>
    )

    /**
     * The list of influences for a new [FixedAreaSpawner] for a PokeSnack.
     * The influences will be added to the spawner in the order they are added to this list.
     */
    var influenceBuilders = mutableListOf<(ctx: Context) -> SpawningInfluence?>(
        { ctx ->
            val stackedLureTier = ctx.baitEffects
                .filter { it.type == SpawnBait.Effects.RARITY_BUCKET }
                .sumOf { it.value }
                .toInt()

            if (stackedLureTier <= 0) null else BucketNormalizingInfluence(
                tier = stackedLureTier,
                gradient = 0.2F,
                firstTier = 1.2F
            )
        },
        { _ ->
            BucketMultiplyingInfluence(
                mapOf(
                    "uncommon" to 2.25f,
                    "rare" to 5.5f,
                    "ultra-rare" to 5.5f,
                )
            )
        },
        { ctx ->
            SpawnBaitInfluence(effects = ctx.baitEffects)
        },
    )

    fun create(ctx: Context): FixedAreaSpawner {
        val influences = influenceBuilders.mapNotNull { it(ctx) }

        return FixedAreaSpawner(
            name = ctx.name,
            spawnPool = spawnPool,
            world = ctx.world,
            position = ctx.pos,
            horizontalRadius = ctx.horizontalRadius,
            verticalRadius = ctx.verticalRadius,
            maxPokemonPerChunk = ctx.maxPokemonPerChunk
        ).also { spawner ->
            spawner.influences.addAll(influences)

            // TODO(Andy): PokeSnacks currently bypass pokemon spawn rules, should that be true?
            // Adding this rule will change that
            spawner.influences.addAll(
                CobblemonSpawnRules.rules.values
                    .filter(SpawnRule::enabled)
                    .flatMap(SpawnRule::components)
            )
        }
    }
}
