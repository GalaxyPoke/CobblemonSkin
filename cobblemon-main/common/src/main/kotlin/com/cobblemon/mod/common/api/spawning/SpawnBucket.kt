/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning

/**
 * A spawn bucket is a simple referenceable object by spawn files. The bucket is used at the beginning of
 * the selection process to decide which set of spawns will be used for spawning on that attempt. Weights are used to
 * make entire buckets more or less likely, and then weighted selections will occur within whichever bucket
 * was chosen.
 *
 * Spawn buckets are configured in the main config.
 *
 * @author Hiroku
 * @since June 20th, 2022
 */
class SpawnBucket() {
    lateinit var name: String
    var weight = 0F

    constructor(name: String, weight: Float): this() {
        this.name = name
        this.weight = weight
    }

    override fun hashCode() = name.hashCode()
    override fun equals(other: Any?) = other is SpawnBucket && other.name == name
}