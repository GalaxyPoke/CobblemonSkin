/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.campfirepot

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity
import com.cobblemon.mod.common.item.CampfirePotItem
import com.cobblemon.mod.common.util.playSoundServer
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.Containers
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import org.jetbrains.annotations.Nullable
import net.minecraft.world.level.block.CampfireBlock as MCCampfireBlock

@Suppress("OVERRIDE_DEPRECATION")
class CampfireBlock(settings: Properties, val isSoul: Boolean) : BaseEntityBlock(settings), SimpleWaterloggedBlock {
    companion object {
        val CODEC: MapCodec<CampfireBlock> = RecordCodecBuilder.mapCodec {
            it.group(
                propertiesCodec(),
                Codec.BOOL.fieldOf("isSoul").forGetter(CampfireBlock::isSoul)
            ).apply(it, ::CampfireBlock)
        }
        val ITEM_DIRECTION = DirectionProperty.create("item_facing")
        val POWERED = BlockStateProperties.POWERED
        val COOKING = BooleanProperty.create("cooking")
        val LID = BooleanProperty.create("lid")

        private val campfireAABB = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.4375, 1.0)
        private val AABB = Shapes.or(
            campfireAABB,
            Shapes.box(0.1875, 0.5, 0.125, 0.875, 0.8125, 0.1875),
            Shapes.box(0.125, 0.4375, 0.125, 0.875, 0.5, 0.875),
            Shapes.box(0.8125, 0.5, 0.1875, 0.875, 0.8125, 0.875),
            Shapes.box(0.125, 0.5, 0.125, 0.1875, 0.8125, 0.8125),
            Shapes.box(0.125, 0.5, 0.8125, 0.8125, 0.8125, 0.875)
        )
    }

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(ITEM_DIRECTION, Direction.NORTH)
            .setValue(POWERED, false)
            .setValue(COOKING, false)
            .setValue(LID, false))
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        var blockState = defaultBlockState()
        val worldView = ctx.level
        val blockPos = ctx.clickedPos
        ctx.nearestLookingDirections.forEach { direction ->
            if (direction.axis.isHorizontal) {
                blockState = blockState
                    .setValue(FACING, direction)
                    .setValue(ITEM_DIRECTION, direction)
                        as BlockState
                if (blockState.canSurvive(worldView, blockPos)) {
                    return blockState
                }
            }
        }
        return null
    }

    override fun getShape(blockState: BlockState, blockGetter: BlockGetter, blockPos: BlockPos, collisionContext: CollisionContext): VoxelShape = AABB

    override fun getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = AABB

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity !is CampfireBlockEntity) {
            return InteractionResult.PASS
        }

        val potItem = blockEntity.getPotItem()?.item as? CampfirePotItem
        if (potItem != null) {
            if (!level.isClientSide) {
                if (player.isCrouching) {
                    removePotItem(blockEntity, state, level, pos, player)
                } else {
                    openContainer(level, pos, player)
                }
            }

            return InteractionResult.SUCCESS
        } else if (player.getItemInHand(InteractionHand.MAIN_HAND).item is CampfirePotItem) {
            val heldItem = player.getItemInHand(InteractionHand.MAIN_HAND)
            blockEntity.setPotItem(heldItem.split(1))

            level.playSoundServer(
                position = pos.bottomCenter,
                sound = CobblemonSounds.CAMPFIRE_POT_PLACE,
            )

            return InteractionResult.SUCCESS
        }

        return InteractionResult.PASS
    }

    override fun placeLiquid(level: LevelAccessor, pos: BlockPos, state: BlockState, fluidState: FluidState): Boolean {
        val blockEntity = level.getBlockEntity(pos)
        if (fluidState.type === Fluids.WATER && blockEntity is CampfireBlockEntity) {
            if (level is Level) {
                if (!level.isClientSide) {
                    removePotItem(blockEntity, state, level, pos, null, true)
                    level.playSoundServer(pos.center, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F)
                }

                if (level.isClientSide) for (i in 0..19) MCCampfireBlock.makeParticles(level, pos, false, true)
            }

            level.scheduleTick(pos, fluidState.type, fluidState.type.getTickDelay(level))
            return true
        } else {
            return false
        }
    }

    private fun removePotItem(blockEntity: CampfireBlockEntity, blockState: BlockState, level: Level, blockPos: BlockPos, player: Player? = null, byWater: Boolean = false) {
        if (!byWater && player != null && !player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty) return

        val potItem = blockEntity.getPotItem()

        if (!byWater && player != null) {
            if (!player.isCreative && potItem != null) {
                player.setItemInHand(InteractionHand.MAIN_HAND, potItem)
            }
        } else {
            val direction = blockState.getValue(FACING) as Direction
            val f = 0.25F * direction.stepX.toFloat()
            val g = 0.25F * direction.stepZ.toFloat()

            val itemEntity = ItemEntity(level, blockPos.x.toDouble() + 0.5 + f.toDouble(), (blockPos.y + 1).toDouble(), blockPos.z.toDouble() + 0.5 + g.toDouble(), potItem)
            itemEntity.setDefaultPickUpDelay()
            level.addFreshEntity(itemEntity)
        }

        blockEntity.setPotItem(ItemStack.EMPTY)
        level.playSoundServer(blockPos.bottomCenter, CobblemonSounds.CAMPFIRE_POT_RETRIEVE)

        Containers.dropContents(level, blockPos, blockEntity)

        val facing = blockState.getValue(FACING)
        blockEntity.setRemoved()

        val newBlockState = if (isSoul) Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(FACING, facing)
            else Blocks.CAMPFIRE.defaultBlockState().setValue(FACING, facing)

        level.setBlockAndUpdate(blockPos, if (byWater) newBlockState.setValue(BlockStateProperties.WATERLOGGED, true).setValue(MCCampfireBlock.LIT, false) else newBlockState)
    }

    fun openContainer(level: Level, pos: BlockPos, player: Player) {
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity is CampfireBlockEntity) player.openMenu(blockEntity)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, ITEM_DIRECTION, POWERED, COOKING, LID)
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

    override fun getRenderShape(state: BlockState) = RenderShape.MODEL

    override fun getAnalogOutputSignal(state: BlockState, level: Level, pos: BlockPos): Int {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos))
    }

    override fun hasAnalogOutputSignal(state: BlockState): Boolean = true

    override fun codec(): MapCodec<out BaseEntityBlock> {
        return CODEC
    }

    override fun isPathfindable(state: BlockState, type: PathComputationType): Boolean = false

    override fun <T : BlockEntity?> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T?>
    ): BlockEntityTicker<T?>? {
        return createCookingPotTicker(level, blockEntityType as BlockEntityType<*>, CobblemonBlockEntities.CAMPFIRE) as BlockEntityTicker<T?>?
    }

    @Nullable
    protected fun <T : BlockEntity> createCookingPotTicker(
        level: Level,
        serverType: BlockEntityType<T>,
        clientType: BlockEntityType<out CampfireBlockEntity>
    ): BlockEntityTicker<T>? {
        return if (level.isClientSide) createTickerHelper(serverType, clientType, CampfireBlockEntity::clientTick)
            else createTickerHelper(serverType, clientType, CampfireBlockEntity::serverTick)
    }

    override fun newBlockEntity(
        pos: BlockPos,
        state: BlockState
    ): BlockEntity? {
        return CampfireBlockEntity(pos, state)
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (random.nextInt(10) == 0) {
            level.playLocalSound(
                pos.x.toDouble() + 0.5,
                pos.y.toDouble() + 0.5,
                pos.z.toDouble() + 0.5,
                SoundEvents.CAMPFIRE_CRACKLE,
                SoundSource.BLOCKS,
                0.5f + random.nextFloat(),
                random.nextFloat() * 0.7f + 0.6f,
                false
            )
        }
    }

    override fun getCloneItemStack(level: LevelReader, pos: BlockPos, state: BlockState): ItemStack {
        return if (isSoul) ItemStack(Blocks.SOUL_CAMPFIRE) else ItemStack(Blocks.CAMPFIRE)
    }

    override fun neighborChanged(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        neighborBlock: Block,
        neighborPos: BlockPos,
        movedByPiston: Boolean
    ) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston)
        val isPowered = level.hasNeighborSignal(pos)
        val blockEntity = level.getBlockEntity(pos) as? CampfireBlockEntity ?: return

        if (isPowered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, isPowered), UPDATE_ALL)
            blockEntity.toggleLid(!isPowered)
        }
    }

    override fun isSignalSource(state: BlockState): Boolean {
        return true
    }

    override fun onRemove(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        newState: BlockState,
        movedByPiston: Boolean
    ) {
        if (!state.`is`(newState.block)) {
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is CampfireBlockEntity) {
                if (level is ServerLevel) {
                    Containers.dropContents(level, pos, blockEntity)
                    val potItem = blockEntity.getPotItem() ?: ItemStack.EMPTY

                    if (!potItem.isEmpty) {
                        val direction = state.getValue(FACING) as Direction
                        val f = 0.25F * direction.stepX.toFloat()
                        val g = 0.25F * direction.stepZ.toFloat()

                        val itemEntity = ItemEntity(level, pos.x.toDouble() + 0.5 + f.toDouble(), (pos.y + 1).toDouble(), pos.z.toDouble() + 0.5 + g.toDouble(), potItem)
                        itemEntity.setDefaultPickUpDelay()

                        level.addFreshEntity(itemEntity)
                    }
                }

                super.onRemove(state, level, pos, newState, movedByPiston)
                level.updateNeighbourForOutputSignal(pos, this)
            } else {
                super.onRemove(state, level, pos, newState, movedByPiston)
            }
        }
    }
}