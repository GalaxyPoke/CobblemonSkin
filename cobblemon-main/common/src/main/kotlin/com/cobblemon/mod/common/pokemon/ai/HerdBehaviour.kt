/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity

class HerdBehaviour {
    /** Who do I consider to not be an idiot? */
    val toleratedLeaders = mutableListOf<ToleratedHerdLeader>()
    /** How many idiots can follow me? */
    val maxSize = 0
    /** How closely am I brown nosing the leader, min to max */
    val followDistance: IntRange = 4..8

    @Transient
    val struct = ObjectValue(this).also {
        it.addFunction("has_tolerated_leaders") { DoubleValue(toleratedLeaders.isNotEmpty()) }
        it.addFunction("max_size") { DoubleValue(maxSize) }
    }

    fun bestMatchLeader(follower: PokemonEntity, possibleLeader: PokemonEntity): ToleratedHerdLeader? {
        var bestTier = 0
        var bestLeader: ToleratedHerdLeader? = null
        for (leader in toleratedLeaders) {
            if (leader.pokemon.matches(possibleLeader) && (leader.ignoresLevel || follower.pokemon.level <= possibleLeader.pokemon.level)) {
                if (leader.tier > bestTier) {
                    bestTier = leader.tier
                    bestLeader = leader
                } else if (leader.tier == bestTier) {
                    bestLeader = leader // Last in list wins
                }
            }
        }
        return bestLeader
    }

    fun initialize() {
        toleratedLeaders.forEach { it.initialize() }
    }
}