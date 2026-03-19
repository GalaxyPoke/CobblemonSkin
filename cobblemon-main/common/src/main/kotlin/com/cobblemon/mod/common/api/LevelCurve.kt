/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api

/**
 * A curve of experience that maps to levels. This is generally a discretized polynomial equation
 * that takes a level as x and returns the amount of experience required to reach it as y.
 *
 * Some curves are too complicated to invert. Implementations can use [CachedLevelThresholds] to
 * workaround this issue.
 *
 * @author Hiroku
 * @since March 21st, 2022
 */
interface LevelCurve {
    fun getExperience(level: Int): Int
    fun getLevel(experience: Int): Int
}

/**
 * A collection of thresholds for what levels require in terms of
 * experience. This is a caching mechanism so that a complicated
 * experience-to-level polynomial can be inverted.
 *
 * @author Hiroku
 * @since March 21st, 2022
 */
class CachedLevelThresholds(
    val levelLimit: Int = 1000,
    val experienceToLevel: (Int) -> Int
) {
    val savedThresholds = mutableListOf<Int>()
    fun getLevel(experience: Int): Int {
        var level = 1
        while (level <= savedThresholds.size) {
            val threshold = savedThresholds[level - 1]
            if (experience < threshold) {
                return level - 1
            }
            level++
        }
        while (level < levelLimit) {
            val threshold = experienceToLevel(level)
            savedThresholds.add(threshold)
            if (experience < threshold) {
                return level - 1
            }
            level++
        }
        return 1
    }
}