/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai

import net.minecraft.world.entity.ai.behavior.BlockPosTracker
import net.minecraft.world.phys.Vec3

/**
 * Wraps around [BlockPosTracker] to add additional flags for Cobblemon-specific behavior.
 *
 * @author Hiroku
 * @since August 4th, 2025
 */
class CobblemonBlockPosTracker(
    pos: Vec3,
    val flags: Set<String> = emptySet(),
) : BlockPosTracker(pos) {
    companion object {
        const val BATTLE_LOOK_BYPASS_FLAG = "battle_look_bypass"
    }

    fun isBattleLookBypass() = flags.contains(BATTLE_LOOK_BYPASS_FLAG)
}