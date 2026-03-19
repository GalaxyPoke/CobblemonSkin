/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.tags.CobblemonBlockTags
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraft.world.level.block.BushBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class NutBushBlock(properties: Properties) : BushBlock(properties), BonemealableBlock {

    override fun codec(): MapCodec<NutBushBlock> = CODEC

    override fun getCloneItemStack(level: LevelReader, pos: BlockPos, state: BlockState): ItemStack {
        return ItemStack(CobblemonItems.GALARICA_NUTS)
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return if (state.getValue(AGE) == 0) {
            SAPLING_SHAPE
        } else {
            if (state.getValue(AGE) < MAX_AGE) MID_GROWTH_SHAPE else super.getShape(state, level, pos, context)
        }
    }

    override fun isRandomlyTicking(state: BlockState): Boolean {
        return state.getValue(AGE) < MAX_AGE
    }

    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        val age = state.getValue(AGE)
        if (world.getRawBrightness(pos.above(), 0) >= 9 && age < MAX_AGE && random.nextInt(8) == 0) {
            val blockstate = state.setValue(AGE, age + 1)
            world.setBlock(pos, blockstate, UPDATE_CLIENTS)
            world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(blockstate))
        }
    }

    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (entity is LivingEntity && entity.type !== EntityType.FOX && entity.type !== EntityType.BEE) {
            entity.makeStuckInBlock(state, Vec3(0.8, 0.75, 0.8))
        }
    }

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): ItemInteractionResult {
        return if (isValidBonemealTarget(level, pos, state) && stack.`is`(Items.BONE_MEAL))
            ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION
        else super.useItemOn(stack, state, level, pos, player, hand, hitResult)
    }

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
        if (state.getValue(AGE) == MAX_AGE) {
            val berryAmount = 1 + level.random.nextInt(2)
            popResource(level, pos, ItemStack(CobblemonItems.GALARICA_NUTS, berryAmount))
            level.playSound(
                null,
                pos,
                SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                SoundSource.BLOCKS,
                1.0f,
                0.8f + level.random.nextFloat() * 0.4f
            )
            val blockstate = state.setValue(AGE, 1)
            level.setBlock(pos, blockstate, UPDATE_CLIENTS)
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, blockstate))
            return InteractionResult.sidedSuccess(level.isClientSide)
        } else {
            return super.useWithoutItem(state, level, pos, player, hitResult)
        }
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(AGE)
    }

    override fun isValidBonemealTarget(level: LevelReader, pos: BlockPos, state: BlockState): Boolean {
        return state.getValue(AGE) < MAX_AGE
    }

    override fun isBonemealSuccess(level: Level, random: RandomSource, pos: BlockPos, state: BlockState): Boolean {
        return true
    }

    override fun performBonemeal(level: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) {
        level.setBlock(
            pos,
            state.setValue(AGE, (state.getValue(AGE) + 1).coerceAtMost(MAX_AGE)),
            UPDATE_CLIENTS
        )
    }

    override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos): Boolean {
        return state.`is`(CobblemonBlockTags.GALARICA_NUT_MAY_PLACE_ON)
    }

    init {
        this.registerDefaultState(
            stateDefinition.any()
                .setValue(AGE, 0)
        )
    }

    companion object {
        val CODEC: MapCodec<NutBushBlock> = simpleCodec(::NutBushBlock)

        const val MAX_AGE: Int = 3
        val AGE: IntegerProperty = BlockStateProperties.AGE_3
        private val SAPLING_SHAPE =
            box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0)
        private val MID_GROWTH_SHAPE =
            box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0)
    }
}