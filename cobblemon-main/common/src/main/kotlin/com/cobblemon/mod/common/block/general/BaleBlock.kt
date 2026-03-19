/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.general

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState


class BaleBlock(properties: Properties, val fallDamageMultiplier: Float = 0.3f) : RotatedPillarBlock(properties) {
    override fun fallOn(level: Level, state: BlockState, pos: BlockPos, entity: Entity, fallDistance: Float) {
        entity.causeFallDamage(fallDistance, fallDamageMultiplier, level.damageSources().fall())
    }
}