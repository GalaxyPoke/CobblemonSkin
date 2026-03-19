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
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.createDuplicateRuntime
import com.cobblemon.mod.common.util.resolveFloat
import net.minecraft.tags.FluidTags
import net.minecraft.tags.TagKey
import net.minecraft.world.level.material.Fluid

class SwimBehaviour {
    val avoidsWater = false

    val canSwimInWater = true
    val swimSpeed = "0.3".asExpression()
    val canBreatheUnderwater = false
    val canWalkOnWater = false

    val canSwimInLava = false
    val canBreatheUnderlava = false
    val canWalkOnLava = false

    fun canWalkOnFluid(tag: TagKey<Fluid>) = if (tag == FluidTags.WATER) canWalkOnWater else if (tag == FluidTags.LAVA) canWalkOnLava else false
    fun canBreatheUnderFluid(tag: TagKey<Fluid>) = if (tag == FluidTags.WATER) canBreatheUnderwater else if (tag == FluidTags.LAVA) canBreatheUnderlava else false
    fun canSwimInFluid(tag: TagKey<Fluid>) = if (tag == FluidTags.WATER) canSwimInWater else if (tag == FluidTags.LAVA) canSwimInLava else false

    @Transient
    val struct = ObjectValue(this).also {
        it.addFunction("avoids_water") { DoubleValue(avoidsWater) }
        it.addFunction("can_swim_in_water") { DoubleValue(canSwimInWater) }
        it.addFunction("can_swim_in_lava") { DoubleValue(canSwimInLava) }
        it.addFunction("swim_speed") { it.environment.createDuplicateRuntime().resolveFloat(swimSpeed) }
        it.addFunction("can_breathe_underwater") { DoubleValue(canBreatheUnderwater) }
        it.addFunction("can_breathe_underlava") { DoubleValue(canBreatheUnderlava) }
        it.addFunction("can_walk_on_water") { DoubleValue(canWalkOnWater) }
        it.addFunction("can_walk_on_lava") { DoubleValue(canWalkOnLava) }
    }
}