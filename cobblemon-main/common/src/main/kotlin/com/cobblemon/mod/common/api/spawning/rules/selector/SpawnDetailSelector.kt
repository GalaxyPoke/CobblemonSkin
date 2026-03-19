/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.rules.selector

import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail

interface SpawnDetailSelector {
    companion object {
        val types = mutableMapOf<String, Class<out SpawnDetailSelector>>()

        inline fun <reified T : SpawnDetailSelector> register(type: String) {
            types[type] = T::class.java
        }
    }
    fun selects(spawnDetail: SpawnDetail): Boolean
}