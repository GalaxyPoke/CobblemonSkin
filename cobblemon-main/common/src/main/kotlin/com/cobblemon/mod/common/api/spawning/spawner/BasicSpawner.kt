/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.spawner

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.spawning.SpawningZoneGenerator
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.api.spawning.position.AreaSpawnablePositionResolver
import com.cobblemon.mod.common.api.spawning.selection.SpawningSelector

/**
 * A basic spawner implementation that uses Cobblemon's default algorithms for zone generation,
 * position resolving, and spawn selection.
 *
 * @author Hiroku
 * @since October 27th, 2025
 */
open class BasicSpawner(
    override val name: String,
    override var spawnPool: SpawnPool,
    override var maxPokemonPerChunk: Float = Cobblemon.config.pokemonPerChunk
) : Spawner {
    override var selector: SpawningSelector<*> = SpawningSelector.DEFAULT
    override var generator: SpawningZoneGenerator = Cobblemon.spawningZoneGenerator
    override var resolver: AreaSpawnablePositionResolver = Cobblemon.areaSpawnablePositionResolver
    override val influences = mutableListOf<SpawningInfluence>()
}