/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity

import com.cobblemon.mod.common.entity.ai.OmniPathNavigation
import com.cobblemon.mod.common.pokemon.ai.OmniPathNodeMaker
import net.minecraft.world.level.material.FluidState

/**
 * Interface used to abstract moving capabilities of an entity to support the [OmniPathNodeMaker] and [OmniPathNavigation]
 *
 * @author Hiroku
 * @since April 18th, 2025
 */
interface OmniPathingEntity {
    fun canWalk(): Boolean
    fun canSwimInWater(): Boolean
    fun canWalkOnWater(): Boolean
    fun canSwimInLava(): Boolean
    fun canWalkOnLava(): Boolean
    fun canSwimUnderFluid(fluidState: FluidState): Boolean
    fun canPathThroughSaccLeaves(): Boolean
    fun canFly(): Boolean
    fun isFlying(): Boolean
    fun setFlying(state: Boolean)
    /** Returns true if the entity would be ok with moving out of the flying state, or if it would prefer to continue flying. */
    fun couldStopFlying(): Boolean
    fun entityOnGround(): Boolean
}