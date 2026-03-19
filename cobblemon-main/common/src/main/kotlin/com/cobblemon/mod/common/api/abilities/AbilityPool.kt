/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.abilities

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.PrioritizedList
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.pokemon.Species

/**
 * A pool of potential abilities, as a [PrioritizedList]. The added logic of this subclass
 * is that it has selection logic. Given a species and a set of aspects, it will go through
 * each priority group, and if it can then it will pick from that group and return it.
 *
 * @author Hiroku
 * @since July 28th, 2022
 */
open class AbilityPool : PrioritizedList<PotentialAbility>() {
    fun select(species: Species, aspects: Set<String>): Pair<Ability, Priority> {
        for (priority in Priority.entries) {
            val potentialAbilities = priorityMap[priority]?.filter { it.isSatisfiedBy(aspects) } ?: continue
            if (potentialAbilities.isNotEmpty()) {
                return potentialAbilities.random().template.create() to priority
            }
        }

        LOGGER.error("Unable to select an ability from the pool for $species and aspects: ${aspects.joinToString()}")
        LOGGER.error("Please show this to the Cobblemon developers!")
        Exception().printStackTrace()
        return Abilities.first().create() to Priority.LOWEST
    }
}