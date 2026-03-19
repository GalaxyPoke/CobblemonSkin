/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.selection

import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition

/**
 * This interface allows the selection algorithms to be relatively free to implement
 * things the way they want while still allowing their process to be altered by things
 * like herd spawning.
 *
 * @author Hiroku
 * @since April 21st, 2025
 */
interface SpawnSelectionData {
    val context: MutableMap<String, Any>
    val spawnActions: MutableList<SpawnAction<*>>
    fun removeSpawnDetails(shouldRemove: (SpawnDetail) -> Boolean)
    fun removeSpawnablePositions(shouldRemove: (SpawnDetail, SpawnablePosition) -> Boolean)

    companion object {
        val EMPTY = object : SpawnSelectionData {
            override val context: MutableMap<String, Any> = mutableMapOf()
            override val spawnActions: MutableList<SpawnAction<*>> = mutableListOf()
            override fun removeSpawnDetails(shouldRemove: (SpawnDetail) -> Boolean) {}
            override fun removeSpawnablePositions(shouldRemove: (SpawnDetail, SpawnablePosition) -> Boolean) {}
        }
    }
}