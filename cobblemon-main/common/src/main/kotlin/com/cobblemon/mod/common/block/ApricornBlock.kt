/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.api.apricorn.Apricorn
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.farming.ApricornHarvestEvent
import com.cobblemon.mod.common.api.tags.CobblemonBlockTags
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.util.playSoundServer
import com.cobblemon.mod.common.util.toVec3d
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.EntityCollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

// Note we cannot make this inherit from CocoaBlock since our age properties differ, it is however safe to copy most of the logic from it
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class ApricornBlock(settings: Properties, val apricorn: Apricorn) : HorizontalDirectionalBlock(settings), BonemealableBlock, ShearableBlock {

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(AGE, MIN_AGE))
    }

    override fun isRandomlyTicking(state: BlockState) = state.getValue(AGE) < MAX_AGE

    @Deprecated("Deprecated in Java")
    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        // Cocoa block uses a 5 here might as well stay consistent
        if (world.random.nextInt(5) == 0) {
            val currentAge = state.getValue(AGE)
            if (currentAge < MAX_AGE) {
                world.setBlock(pos, state.setValue(AGE, currentAge + 1), 2)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos): Boolean {
        val blockState = world.getBlockState(pos.relative(state.getValue(FACING) as Direction))
        return blockState.`is`(CobblemonBlockTags.APRICORN_LEAVES)
    }

    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        val age = state.getValue(AGE)
        return when (state.getValue(FACING)) {
            Direction.NORTH -> NORTH_AABB[age]
            Direction.EAST -> EAST_AABB[age]
            Direction.SOUTH -> SOUTH_AABB[age]
            Direction.WEST -> WEST_AABB[age]
            else -> NORTH_AABB[age]
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getCollisionShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        if (context is EntityCollisionContext && (context.entity as? ItemEntity)?.item?.`is`(CobblemonItemTags.APRICORNS) == true) {
            return Shapes.empty()
        }
        return super.getCollisionShape(state, world, pos, context)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        var blockState = defaultBlockState()
        val worldView = ctx.level
        val blockPos = ctx.clickedPos
        ctx.nearestLookingDirections.forEach { direction ->
            if (direction.axis.isHorizontal) {
                blockState = blockState.setValue(FACING, direction) as BlockState
                if (blockState.canSurvive(worldView, blockPos)) {
                    return blockState
                }
            }
        }
        return null
    }

    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        return if (direction == state.getValue(FACING) && !state.canSurvive(world, pos)) Blocks.AIR.defaultBlockState()
            else super.updateShape(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun isValidBonemealTarget(world: LevelReader, pos: BlockPos, state: BlockState) = state.getValue(AGE) < MAX_AGE

    override fun isBonemealSuccess(world: Level, random: RandomSource, pos: BlockPos, state: BlockState) = true

    override fun performBonemeal(world: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
        val curAge = state.getValue(AGE)
        val newAge = curAge + 1
        if (newAge > MAX_AGE) return
        world.setBlock(pos, state.setValue(AGE, newAge), UPDATE_CLIENTS)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, AGE)
    }

    override fun isPathfindable(state: BlockState, type: PathComputationType): Boolean = false

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        blockHitResult: BlockHitResult
    ): InteractionResult {
        if (state.getValue(AGE) != MAX_AGE) {
            return super.useWithoutItem(state, level, pos, player, blockHitResult)
        }

        doHarvest(level, state, pos, player)
        return InteractionResult.SUCCESS
    }

    override fun attack(state: BlockState, world: Level, pos: BlockPos, player: Player) {
        if (state.getValue(AGE) != MAX_AGE) {
            return super.attack(state, world, pos, player)
        }

        doHarvest(world, state, pos, player)
    }

    // We need to point back to the actual apricorn item, see SweetBerryBushBlock for example
    override fun getCloneItemStack(world: LevelReader, pos: BlockPos, state: BlockState) = ItemStack(this.apricorn.item())

    private fun doHarvest(world: Level, state: BlockState, pos: BlockPos, player: Player) {
        val resetState = this.harvest(world, state, pos)
        world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, resetState))

        if (!world.isClientSide) {
            world.playSoundServer(position = pos.toVec3d(), sound = SoundEvents.ITEM_PICKUP, volume = 0.7F, pitch = 1.4F)

            if (world is ServerLevel && player is ServerPlayer) {
                CobblemonEvents.APRICORN_HARVESTED.post(ApricornHarvestEvent(player, apricorn, world, pos))
            }
        }
    }

    /**
     * Harvests the apricorn at the given params.
     * This uses [Block.dropResources] to handle the drops.
     * It will also reset the [BlockState] of this block at the given location to the start of growth.
     *
     * @param world The [Level] the apricorn is in.
     * @param state The [BlockState] of the apricorn.
     * @param pos The [BlockPos] of the apricorn.
     * @return The [BlockState] after harvest.
     */
    fun harvest(world: Level, state: BlockState, pos: BlockPos): BlockState {
        // Uses loot tables, to change the drops use 'data/cobblemon/loot_tables/blocks/<color>_apricorn.json'
        dropResources(state, world, pos)
        // Don't use default as we want to keep the facing
        val resetState = state.setValue(AGE, MIN_AGE)
        world.setBlock(pos, resetState, UPDATE_CLIENTS)
        return resetState
    }

    override fun attemptShear(world: Level, state: BlockState, pos: BlockPos, successCallback: () -> Unit): Boolean {
        if (state.getValue(AGE) != MAX_AGE) {
            return false
        }
        world.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1F, 1F)
        this.harvest(world, state, pos)
        successCallback()
        world.gameEvent(null, GameEvent.SHEAR, pos)
        return true
    }

    override fun codec(): MapCodec<out HorizontalDirectionalBlock> {
        return CODEC
    }

    companion object {
        val CODEC: MapCodec<ApricornBlock> = RecordCodecBuilder.mapCodec { it.group(
            propertiesCodec(),
            Apricorn.CODEC.fieldOf("apricorn").forGetter(ApricornBlock::apricorn)
        ).apply(it, ::ApricornBlock) }

        val AGE = BlockStateProperties.AGE_3
        const val MAX_AGE = BlockStateProperties.MAX_AGE_3
        const val MIN_AGE = 0

        // North
        // Stage 0
        private val NORTH_STAGE_0 = Shapes.box(0.40625, 0.5, 0.0, 0.59375, 0.6875, 0.1875)
        private val NORTH_CAP_STAGE_0 = Shapes.box(0.4375, 0.65625, 0.03125, 0.5625, 0.71875, 0.15625)

        // Stage 1
        private val NORTH_STAGE_1 = Shapes.box(0.375, 0.40625, 0.0, 0.625, 0.65625, 0.25)
        private val NORTH_CAP_STAGE_1 = Shapes.box(0.40625, 0.625, 0.03125, 0.59375, 0.6875, 0.21875)

        // Stage 2
        private val NORTH_STAGE_2 = Shapes.box(0.34375, 0.296875, 0.0, 0.65625, 0.609375, 0.3125)
        private val NORTH_CAP_STAGE_2 = Shapes.box(0.375, 0.59375, 0.03125, 0.625, 0.65625, 0.28125)

        // Stage 3 - Full Fruit
        private val NORTH_STAGE_3 = Shapes.box(0.3125, 0.1875, 0.0, 0.6875, 0.5625, 0.375)
        private val NORTH_CAP_STAGE_3 = Shapes.box(0.375, 0.5625, 0.0625, 0.625, 0.625, 0.3125)

        private val NORTH_AABB = arrayOf(
            Shapes.or(NORTH_STAGE_0, NORTH_CAP_STAGE_0),
            Shapes.or(NORTH_STAGE_1, NORTH_CAP_STAGE_1),
            Shapes.or(NORTH_STAGE_2, NORTH_CAP_STAGE_2),
            Shapes.or(NORTH_STAGE_3, NORTH_CAP_STAGE_3)
        )

        // South
        // Stage 0
        private val SOUTH_STAGE_0 = Shapes.box(0.40625, 0.5, 0.8125, 0.59375, 0.6875, 1.0)
        private val SOUTH_CAP_STAGE_0 = Shapes.box(0.4375, 0.65625, 0.84375, 0.5625, 0.71875, 0.96875)

        // Stage 1
        private val SOUTH_STAGE_1 = Shapes.box(0.375, 0.40625, 0.75, 0.625, 0.65625, 1.0)
        private val SOUTH_CAP_STAGE_1 = Shapes.box(0.40625, 0.625, 0.78125, 0.59375, 0.6875, 0.96875)

        // Stage 2
        private val SOUTH_STAGE_2 = Shapes.box(0.34375, 0.296875, 0.6875, 0.65625, 0.609375, 1.0)
        private val SOUTH_CAP_STAGE_2 = Shapes.box(0.375, 0.59375, 0.71875, 0.625, 0.65625, 0.96875)

        // Stage 3 - Full Fruit
        private val SOUTH_STAGE_3 = Shapes.box(0.3125, 0.1875, 0.625, 0.6875, 0.5625, 1.0)
        private val SOUTH_CAP_STAGE_3 = Shapes.box(0.375, 0.5625, 0.6875, 0.625, 0.625, 0.9375)

        private val SOUTH_AABB = arrayOf(
            Shapes.or(SOUTH_STAGE_0, SOUTH_CAP_STAGE_0),
            Shapes.or(SOUTH_STAGE_1, SOUTH_CAP_STAGE_1),
            Shapes.or(SOUTH_STAGE_2, SOUTH_CAP_STAGE_2),
            Shapes.or(SOUTH_STAGE_3, SOUTH_CAP_STAGE_3)
        )

        // East
        // Stage 0
        private val EAST_STAGE_0 = Shapes.box(0.8125, 0.5, 0.40625, 1.0, 0.6875, 0.59375)
        private val EAST_CAP_STAGE_0 = Shapes.box(0.84375, 0.65625, 0.4375, 0.96875, 0.71875, 0.5625)

        // Stage 1
        private val EAST_STAGE_1 = Shapes.box(0.75, 0.40625, 0.375, 1.0, 0.65625, 0.625)
        private val EAST_CAP_STAGE_1 = Shapes.box(0.78125, 0.625, 0.40625, 0.96875, 0.6875, 0.59375)

        // Stage 2
        private val EAST_STAGE_2 = Shapes.box(0.6875, 0.296875, 0.34375, 1.0, 0.609375, 0.65625)
        private val EAST_CAP_STAGE_2 = Shapes.box(0.71875, 0.59375, 0.375, 0.96875, 0.65625, 0.625)

        // Stage 3 - Full Fruit
        private val EAST_STAGE_3 = Shapes.box(0.625, 0.1875, 0.3125, 1.0, 0.5625, 0.6875)
        private val EAST_CAP_STAGE_3 = Shapes.box(0.6875, 0.5625, 0.375, 0.9375, 0.625, 0.625)

        private val EAST_AABB = arrayOf(
            Shapes.or(EAST_STAGE_0, EAST_CAP_STAGE_0),
            Shapes.or(EAST_STAGE_1, EAST_CAP_STAGE_1),
            Shapes.or(EAST_STAGE_2, EAST_CAP_STAGE_2),
            Shapes.or(EAST_STAGE_3, EAST_CAP_STAGE_3)
        )

        // West
        // Stage 0
        private val WEST_STAGE_0 = Shapes.box(0.0, 0.5, 0.40625, 0.1875, 0.6875, 0.59375)
        private val WEST_CAP_STAGE_0 = Shapes.box(0.03125, 0.65625, 0.4375, 0.15625, 0.71875, 0.5625)

        // Stage 1
        private val WEST_STAGE_1 = Shapes.box(0.0, 0.40625, 0.375, 0.25, 0.65625, 0.625)
        private val WEST_CAP_STAGE_1 = Shapes.box(0.03125, 0.625, 0.40625, 0.21875, 0.6875, 0.59375)

        // Stage 2
        private val WEST_STAGE_2 = Shapes.box(0.0, 0.296875, 0.34375, 0.3125, 0.609375, 0.65625)
        private val WEST_CAP_STAGE_2 = Shapes.box(0.03125, 0.59375, 0.375, 0.28125, 0.65625, 0.625)

        // Stage 3 - Full Fruit
        private val WEST_STAGE_3 = Shapes.box(0.0, 0.1875, 0.3125, 0.375, 0.5625, 0.6875)
        private val WEST_CAP_STAGE_3 = Shapes.box(0.0625, 0.5625, 0.375, 0.3125, 0.625, 0.625)

        private val WEST_AABB = arrayOf(
            Shapes.or(WEST_STAGE_0, WEST_CAP_STAGE_0),
            Shapes.or(WEST_STAGE_1, WEST_CAP_STAGE_1),
            Shapes.or(WEST_STAGE_2, WEST_CAP_STAGE_2),
            Shapes.or(WEST_STAGE_3, WEST_CAP_STAGE_3)
        )

    }


}