/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.influence

import com.cobblemon.mod.common.api.spawning.SpawningZone
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition

/**
 * A wrapping around a [SpawningInfluence] that was produced for a [SpawningZone]. This is wrapped
 * to explicitly state that some are [UnconditionalSpawningZoneInfluence] and therefore apply to all the
 * spawnable positions and any overarching operations (like bucket selection) whereas others are
 * [ConditionalSpawningZoneInfluence] and may only apply to particular [SpawnablePosition]s.
 *
 * The only reason this cannot be simplified to all being [ConditionalSpawningZoneInfluence] is because
 * some influence functions can be run after the spawning zone has been created but outside the context
 * of any particular spawnable positions. I don't want to run it with a fake spawnable position to test
 * whether it's effectively unconditional.
 *
 * @author Hiroku
 * @since March 9th, 2025
 */
sealed interface SpawningZoneInfluence {
    val influence: SpawningInfluence
}

open class UnconditionalSpawningZoneInfluence(override val influence: SpawningInfluence) : SpawningZoneInfluence
interface ConditionalSpawningZoneInfluence: SpawningZoneInfluence {
    /** Whether this influence will apply to */
    fun appliesTo(spawnablePosition: SpawnablePosition): Boolean = true
}
