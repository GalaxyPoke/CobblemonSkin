/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.entity

import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.spawner.Spawner

/**
 * Event fired when the spawning system makes a decision on which bucket to spawn from.
 */
class SpawnBucketChosenEvent(
    /** The spawner this is happening within. */
    val spawner: Spawner,
    /** The cause of the spawn. */
    val spawnCause: SpawnCause,
    /** The bucket that was chosen. You can alter this. */
    var bucket: SpawnBucket,
    /** The final weights that were used to make the choice. */
    val bucketWeights: Map<SpawnBucket, Float>
)