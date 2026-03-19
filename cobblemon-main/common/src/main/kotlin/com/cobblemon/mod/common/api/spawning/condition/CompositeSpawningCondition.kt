/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import net.minecraft.core.Holder
import net.minecraft.world.level.biome.Biome

/**
 * A spawning condition that is composed of a list of conditions and anticonditions.
 *
 * A composite condition passes if both of the following are true:
 * - The conditions list is empty, or any of the conditions match.
 * - The anticonditions list is empty, or none of the anticonditions match.
 *
 * In other words, if the anticonditions list has five elements, then only one of those
 * needs to be true for the spawn to be canceled. Meanwhile, if there are five elements
 * in the conditions list, then all of them would need to be false for the spawn to be
 * canceled.
 *
 * @author Hiroku
 * @since January 26th, 2022
 */
class CompositeSpawningCondition {
    var conditions = mutableListOf<SpawningCondition<*>>()
    var anticonditions = mutableListOf<SpawningCondition<*>>()

    fun isBiomeValid(holder: Holder<Biome>): Boolean {
        if (conditions.isEmpty() || conditions.any { it.biomes == null || it.biomes!!.isEmpty() || it.biomes!!.any { it.fits(holder) } }) {
            if (anticonditions.isEmpty() || anticonditions.none { it.biomes != null && it.biomes!!.any { it.fits(holder) } }) {
                return true
            }
        }
        return false
    }

    fun satisfiedBy(spawnablePosition: SpawnablePosition): Boolean {
        return if (conditions.isNotEmpty() && conditions.none { it.isSatisfiedBy(spawnablePosition) }) {
            false
        } else {
            !(anticonditions.isNotEmpty() && anticonditions.any { it.isSatisfiedBy(spawnablePosition) })
        }
    }
}