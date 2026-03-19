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

/**
 * Behavioural properties relating to a Pokémon's ability to look and move.
 *
 * @author Hiroku
 * @since July 30th, 2022
 */
class MoveBehaviour {
    val walk = WalkBehaviour()
    val swim = SwimBehaviour()
    val fly = FlyBehaviour()
    val stepHeight = 0.6F
    val wanderChance = 120
    val wanderSpeed = 1.0
    val canLook = true

    @Transient
    val struct = ObjectValue<MoveBehaviour>(this).also {
        it.addFunction("walk") { walk.struct }
        it.addFunction("swim") { swim.struct }
        it.addFunction("fly") { fly.struct }
        it.addFunction("step_height") { DoubleValue(stepHeight) }
        it.addFunction("wander_chance") { DoubleValue(wanderChance) }
        it.addFunction("wander_speed") { DoubleValue(wanderSpeed) }
        it.addFunction("can_look") { DoubleValue(canLook) }
        it.addFunction("can_move") { DoubleValue(walk.canWalk || swim.canSwimInWater || swim.canSwimInLava || fly.canFly) }
    }
}