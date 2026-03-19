/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.createDuplicateRuntime
import com.cobblemon.mod.common.util.resolveFloat

class WalkBehaviour {
    val canWalk = true
    val avoidsLand = false
    var walkSpeed: Expression = "0.35".asExpression()

    @Transient
    val struct = ObjectValue(this).also {
        it.addFunction("can_walk") { DoubleValue(canWalk) }
        it.addFunction("avoids_land") { DoubleValue(avoidsLand) }
        it.addFunction("walk_speed") { it.environment.createDuplicateRuntime().resolveFloat(walkSpeed) }
    }
}