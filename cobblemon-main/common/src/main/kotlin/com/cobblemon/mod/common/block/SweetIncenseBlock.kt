/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.block.entity.SweetIncenseBlockEntity
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.SimpleWaterloggedBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class SweetIncenseBlock(properties: Properties) : BaseEntityBlock(properties), SimpleWaterloggedBlock {

    companion object {
        val LIT: BooleanProperty = BlockStateProperties.LIT
        val WATERLOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED
    }

    init {
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LIT, false)
                .setValue(WATERLOGGED, false)
        )
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(LIT, WATERLOGGED)
    }

    override fun getLightBlock(state: BlockState, level: BlockGetter, pos: BlockPos): Int {
        return super.getLightBlock(state, level, pos)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        return this.defaultBlockState()
                .setValue(LIT, false)
                .setValue(WATERLOGGED, context.level.getFluidState(context.clickedPos).type === Fluids.WATER)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return SweetIncenseBlockEntity(pos, state)
    }

    val CODEC: MapCodec<SweetIncenseBlock> = RecordCodecBuilder.mapCodec { instance ->
        instance.group(
                Codec.BOOL.fieldOf("lit").forGetter { it.defaultBlockState().getValue(LIT) },
                propertiesCodec()
        ).apply(instance) { lit, properties ->
            SweetIncenseBlock(properties).apply {
                registerDefaultState(this.defaultBlockState().setValue(LIT, lit))
            }
        }
    }

    override fun codec(): MapCodec<out SweetIncenseBlock> {
        return CODEC
    }

    override fun getRenderShape(state: BlockState) = RenderShape.MODEL

    override fun useItemOn(
            stack: ItemStack,
            state: BlockState,
            level: Level,
            pos: BlockPos,
            player: Player,
            hand: InteractionHand,
            hit: BlockHitResult
    ): ItemInteractionResult {
        val heldItem = player.getItemInHand(hand)

        return if (!state.getValue(LIT) && (heldItem.`is`(Items.FLINT_AND_STEEL) || heldItem.`is`(Items.FIRE_CHARGE))) {
            level.setBlock(pos, state.setValue(LIT, true), 3)
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 1.0f)
            ItemInteractionResult.sidedSuccess(level.isClientSide)
        } else if (state.getValue(LIT)) {
            level.setBlock(pos, state.setValue(LIT, false), 3)
            ItemInteractionResult.sidedSuccess(level.isClientSide)
        } else {
            ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        }
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (state.getValue(LIT)) {
            level.addParticle(
                    ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                    pos.x + 0.5 + random.nextDouble() / 3.0 * if (random.nextBoolean()) 1 else -1,
                    pos.y + random.nextDouble() + random.nextDouble(),
                    pos.z + 0.5 + random.nextDouble() / 3.0 * if (random.nextBoolean()) 1 else -1,
                    0.0, 0.07, 0.0
            )
        }
    }
}
