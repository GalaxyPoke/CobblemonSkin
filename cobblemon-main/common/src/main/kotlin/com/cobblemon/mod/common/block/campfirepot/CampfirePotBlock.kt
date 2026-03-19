/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.campfirepot

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.SimpleWaterloggedBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class CampfirePotBlock(settings: Properties) : HorizontalDirectionalBlock(settings), SimpleWaterloggedBlock {

    companion object {
        val CODEC: MapCodec<CampfirePotBlock> = RecordCodecBuilder.mapCodec { it.group(propertiesCodec()).apply(it, ::CampfirePotBlock) }
        val OPEN = BlockStateProperties.OPEN
        val OCCUPIED = BlockStateProperties.OCCUPIED


        private val AABB_NS = Shapes.or(
            Shapes.box(0.125, 0.0, 0.125, 0.875, 0.375, 0.875),
            Shapes.box(0.0, 0.3125, 0.375, 0.125, 0.375, 0.625),
            Shapes.box(0.875, 0.3125, 0.375, 1.0, 0.375, 0.625),
            Shapes.box(0.4375, 0.375, 0.4375, 0.5625, 0.4375, 0.5625)
        )

        private val AABB_WE = Shapes.or(
            Shapes.box(0.125, 0.0, 0.125, 0.875, 0.375, 0.875),
            Shapes.box(0.375, 0.3125, 0.0, 0.625, 0.375, 0.125),
            Shapes.box(0.375, 0.3125, 0.875, 0.625, 0.375, 1.0),
            Shapes.box(0.4375, 0.375, 0.4375, 0.5625, 0.4375, 0.5625),
        )

        private val AABB_NS_OPEN = Shapes.or(
            Shapes.box(0.1875, 0.0625, 0.125, 0.875, 0.375, 0.1875),
            Shapes.box(0.125, 0.0, 0.125, 0.875, 0.0625, 0.875),
            Shapes.box(0.8125, 0.0625, 0.1875, 0.875, 0.375, 0.875),
            Shapes.box(0.125, 0.0625, 0.125, 0.1875, 0.375, 0.8125),
            Shapes.box(0.125, 0.0625, 0.8125, 0.8125, 0.375, 0.875),
            Shapes.box(0.0, 0.3125, 0.375, 0.125, 0.375, 0.625),
            Shapes.box(0.875, 0.3125, 0.375, 1.0, 0.375, 0.625)
        )

        private val AABB_WE_OPEN = Shapes.or(
            Shapes.box(0.1875, 0.0625, 0.125, 0.875, 0.375, 0.1875),
            Shapes.box(0.125, 0.0, 0.125, 0.875, 0.0625, 0.875),
            Shapes.box(0.8125, 0.0625, 0.1875, 0.875, 0.375, 0.875),
            Shapes.box(0.125, 0.0625, 0.125, 0.1875, 0.375, 0.8125),
            Shapes.box(0.125, 0.0625, 0.8125, 0.8125, 0.375, 0.875),
            Shapes.box(0.375, 0.3125, 0.0, 0.625, 0.375, 0.125),
            Shapes.box(0.375, 0.3125, 0.875, 0.625, 0.375, 1.0)
        )
    }

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(OPEN, false)
            .setValue(OCCUPIED, false)
            .setValue(WATERLOGGED, false)
        )
    }

    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        val direction = state.getValue(FACING)
        val open = state.getValue(OPEN)
        return if (open)
            if (direction == Direction.NORTH || direction == Direction.SOUTH) AABB_NS_OPEN else AABB_WE_OPEN
        else
            if (direction == Direction.NORTH || direction == Direction.SOUTH) AABB_NS else AABB_WE
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        var blockState = defaultBlockState().setValue(WATERLOGGED, ctx.level.getFluidState(ctx.clickedPos).type == Fluids.WATER)
        val worldView = ctx.level
        val blockPos = ctx.clickedPos
        ctx.nearestLookingDirections.forEach { direction ->
            if (direction.axis.isHorizontal) {
                blockState = blockState.setValue(FACING, direction.opposite) as BlockState
                if (blockState.canSurvive(worldView, blockPos)) {
                    return blockState
                }
            }
        }
        return null
    }

    override fun codec(): MapCodec<out HorizontalDirectionalBlock> = CODEC

    override fun updateShape(state: BlockState, direction: Direction, neighborState: BlockState, world: LevelAccessor, pos: BlockPos, neighborPos: BlockPos): BlockState {
        if (state.getValue(WATERLOGGED)) {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world))
        }

        return if (direction == state.getValue(FACING) && !state.canSurvive(world, pos)) Blocks.AIR.defaultBlockState()
            else super.updateShape(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, OPEN, OCCUPIED, WATERLOGGED)
    }

    override fun getFluidState(blockState: BlockState): FluidState? {
        return if (blockState.getValue(WATERLOGGED)) Fluids.WATER.getSource(false) else super.getFluidState(blockState);
    }
}
