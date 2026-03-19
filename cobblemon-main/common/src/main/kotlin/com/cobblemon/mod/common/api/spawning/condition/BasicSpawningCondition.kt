/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition

/**
 * A basic spawning condition that works for any type of spawnable position.
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
class BasicSpawningCondition : SpawningCondition<SpawnablePosition>() {
    override fun spawnablePositionClass(): Class<out SpawnablePosition> = SpawnablePosition::class.java
    companion object {
        const val NAME = "basic"
    }
}