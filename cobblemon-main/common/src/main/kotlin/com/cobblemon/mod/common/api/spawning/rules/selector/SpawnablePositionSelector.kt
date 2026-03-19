/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.rules.selector

import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition

interface SpawnablePositionSelector {
    companion object {
        val types = mutableMapOf<String, Class<out SpawnablePositionSelector>>()

        inline fun <reified T : SpawnablePositionSelector> register(type: String) {
            types[type] = T::class.java
        }
    }

    fun selects(spawnablePosition: SpawnablePosition): Boolean
}