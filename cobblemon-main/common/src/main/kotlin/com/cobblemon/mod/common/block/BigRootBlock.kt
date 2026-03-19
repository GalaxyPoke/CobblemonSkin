/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

@Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
class BigRootBlock(settings: Properties) : RootBlock(settings) {
    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = AABB

    override fun shearedResultingState(): BlockState = Blocks.HANGING_ROOTS.defaultBlockState()

    override fun shearedDrop(): ItemStack = Items.STRING.defaultInstance

    override fun codec(): MapCodec<out Block> {
        return CODEC
    }

    companion object {
        val CODEC = simpleCodec(::BigRootBlock)

        private val AABB = Shapes.box(0.2, 0.3, 0.2, 0.8, 1.0, 0.8)
    }

}