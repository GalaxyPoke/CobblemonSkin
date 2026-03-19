/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning

/**
 * A simple utility for combining multiple integer ranges.
 *
 * @author Hiroku
 * @since December 16th, 2022
 */
open class IntRanges() {
    constructor(vararg ranges: IntRange) : this() {
        this.ranges = ranges.toMutableList()
    }

    var ranges = mutableListOf<IntRange>()

    operator fun contains(value: Int) = ranges.any { value in it }
}