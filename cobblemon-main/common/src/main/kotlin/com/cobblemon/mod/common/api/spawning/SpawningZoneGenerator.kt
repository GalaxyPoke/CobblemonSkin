/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning

import com.cobblemon.mod.common.api.spawning.position.AreaSpawnablePosition
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.api.spawning.spawner.SpawningZoneInput

/**
 * Interface responsible for slicing out an async-save [SpawningZone] that can be used for generating
 * [SpawnablePosition]s, specifically [AreaSpawnablePosition]s.
 *
 * @author Hiroku
 * @since January 29th, 2022
 */
interface SpawningZoneGenerator {
    fun generate(spawner: Spawner, input: SpawningZoneInput): SpawningZone
}