/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonBlocks
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.dispenser.DispenseItemBehavior
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.BlockHitResult
import kotlin.random.Random


class SaccharineLogSlatheredBlock(properties: Properties) : HorizontalDirectionalBlock(properties) {
    companion object {
        const val HONEY_TYPE_MAX = 5

        val CODEC: MapCodec<SaccharineLogSlatheredBlock> = simpleCodec(::SaccharineLogSlatheredBlock)
        val HONEY_TYPE: IntegerProperty = IntegerProperty.create("honey_type", 0, HONEY_TYPE_MAX)

        fun createBehavior(): DispenseItemBehavior {
            return DispenseItemBehavior { source, stack ->
                val level = source.level
                val pos = source.pos.relative(source.state.getValue(DispenserBlock.FACING))
                val blockState = level.getBlockState(pos)

                val waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER).item

                if (blockState.block is SaccharineLogSlatheredBlock && stack.`is`(waterBottle)) {
                    val newState = CobblemonBlocks.SACCHARINE_LOG.defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, blockState.getValue(RotatedPillarBlock.AXIS))
                    SaccharineLogBlock.changeLogTypeDispenser(level, pos, newState, stack, source)
                }
                stack
            }
        }
    }

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                .setValue(HONEY_TYPE, 0)
        )
    }

    override fun codec() = CODEC

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        val itemStack = player.getItemInHand(hand)
        val waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER).item
        val blockFace = hitResult.direction

        if (itemStack.`is`(waterBottle) && state.getValue(FACING) == blockFace) {
            if (!level.isClientSide) {
                // Replace the honey with the block variant
                val newState = CobblemonBlocks.SACCHARINE_LOG.defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS))
                level.playSound(null, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS)
                SaccharineLogBlock.changeLogType(level, pos, newState, player, itemStack)
            }

            spawnParticlesAtBlockFace(ParticleTypes.SPLASH, level, pos, blockFace, 40)
            return ItemInteractionResult.SUCCESS
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val direction = (context.player?.direction ?: Direction.NORTH).opposite
        if (direction != Direction.UP && direction != Direction.DOWN) {
            val randomType = Random.nextInt(0, HONEY_TYPE_MAX + 1)
            return defaultBlockState().setValue(FACING, direction).setValue(HONEY_TYPE, randomType)
        }
        return super.getStateForPlacement(context)
    }

    override fun neighborChanged(state: BlockState, level: Level, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos,  movedByPiston: Boolean) {
        val facingDirection = state.getValue(FACING)
        val targetPos = pos.relative(facingDirection)

        if (neighborPos == targetPos) {
            val fluidState = level.getFluidState(neighborPos)
            // Revert log to non-honey block if touching fluid
            if (fluidState.amount > 3) {
                val newState = CobblemonBlocks.SACCHARINE_LOG.defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS))
                level.playSound(null, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS)
                SaccharineLogBlock.changeLogType(level, pos, newState)
            }
        }

        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
        builder.add(RotatedPillarBlock.AXIS)
        builder.add(HONEY_TYPE)
    }

    fun spawnParticlesAtBlockFace(particle: ParticleOptions, level: Level, pos: BlockPos, direction: Direction, amount: Int) {
        val random = level.random
        repeat(amount) {
            val (posX, posY, posZ) = when (direction) {
                Direction.UP -> Triple(pos.x + random.nextDouble(), pos.y + 1.0, pos.z + random.nextDouble())
                Direction.DOWN -> Triple(pos.x + random.nextDouble(), pos.y + 0.0, pos.z + random.nextDouble())
                Direction.NORTH -> Triple(pos.x + random.nextDouble(), pos.y + random.nextDouble(), pos.z + 0.0)
                Direction.SOUTH -> Triple(pos.x + random.nextDouble(), pos.y + random.nextDouble(), pos.z + 1.0)
                Direction.EAST -> Triple(pos.x + 1.0, pos.y + random.nextDouble(), pos.z + random.nextDouble())
                Direction.WEST -> Triple(pos.x + 0.0, pos.y + random.nextDouble(), pos.z + random.nextDouble())
                else -> null
            } ?: return@repeat

            level.addParticle(particle, posX, posY, posZ, 0.0, 0.0, 0.0)
        }
    }
}
