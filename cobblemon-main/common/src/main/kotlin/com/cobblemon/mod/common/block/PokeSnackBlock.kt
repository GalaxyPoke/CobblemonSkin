/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.block.entity.PokeSnackBlockEntity
import com.cobblemon.mod.common.util.rotateShape
import com.cobblemon.mod.common.util.toEquipmentSlot
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ColorParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.tags.ItemTags
import net.minecraft.util.FastColor
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import kotlin.math.floor
import kotlin.random.Random

class PokeSnackBlock(settings: Properties, val isLure: Boolean): BaseEntityBlock(settings) {
    companion object {
        const val MAX_BITES = 8
        const val CAKE_HEIGHT = 0.4375

        val CANDLE_PARTICLE_POSITION = Vec3(0.5, CAKE_HEIGHT + 0.5, 0.5)

        val BITES: IntegerProperty = IntegerProperty.create("bites", 0, MAX_BITES)
        val CANDLE: IntegerProperty = IntegerProperty.create("candle", 0, MapColor.MATERIAL_COLORS.size - 1)

        val idToCandleBlock: Map<Int, Block> = mapOf(
            MapColor.WOOL.id to Blocks.WHITE_CANDLE,
            MapColor.COLOR_ORANGE.id to Blocks.ORANGE_CANDLE,
            MapColor.COLOR_MAGENTA.id to Blocks.MAGENTA_CANDLE,
            MapColor.COLOR_LIGHT_BLUE.id to Blocks.LIGHT_BLUE_CANDLE,
            MapColor.COLOR_YELLOW.id to Blocks.YELLOW_CANDLE,
            MapColor.COLOR_LIGHT_GREEN.id to Blocks.LIME_CANDLE,
            MapColor.COLOR_PINK.id to Blocks.PINK_CANDLE,
            MapColor.COLOR_GRAY.id to Blocks.GRAY_CANDLE,
            MapColor.COLOR_LIGHT_GRAY.id to Blocks.LIGHT_GRAY_CANDLE,
            MapColor.COLOR_CYAN.id to Blocks.CYAN_CANDLE,
            MapColor.COLOR_PURPLE.id to Blocks.PURPLE_CANDLE,
            MapColor.COLOR_BLUE.id to Blocks.BLUE_CANDLE,
            MapColor.COLOR_BROWN.id to Blocks.BROWN_CANDLE,
            MapColor.COLOR_GREEN.id to Blocks.GREEN_CANDLE,
            MapColor.COLOR_RED.id to Blocks.RED_CANDLE,
            MapColor.COLOR_BLACK.id to Blocks.BLACK_CANDLE
        )

        fun getCandleById(id: Int): Block = idToCandleBlock[id] ?: Blocks.CANDLE
        fun getIdByCandle(candle: CandleBlock): Int = (idToCandleBlock.entries.associate { (id, block) -> block to id })[candle] ?: MapColor.SAND.id

        private val CANDLE_SHAPE = Shapes.box(0.4375, CAKE_HEIGHT, 0.4375, 0.5625, 0.8125, 0.5625)

        private val SOUTH_SHAPES = listOf(
            Shapes.box(0.0625, 0.0, 0.0625, 0.9375, CAKE_HEIGHT, 0.9375),
            Shapes.or(
                Shapes.box(0.3125, 0.0, 0.0625, 0.9375, CAKE_HEIGHT, 0.9375),
                Shapes.box(0.0625, 0.0, 0.3125, 0.9375, CAKE_HEIGHT, 0.9375)
            ),
            Shapes.or(
                Shapes.box(0.625, 0.0, 0.0625, 0.9375, CAKE_HEIGHT, 0.3125),
                Shapes.box(0.0625, 0.0, 0.3125, 0.9375, CAKE_HEIGHT, 0.9375)
            ),
            Shapes.box(0.0625, 0.0, 0.3125, 0.9375, CAKE_HEIGHT, 0.9375),
            Shapes.or(
                Shapes.box(0.6875, 0.0, 0.625, 0.9375, CAKE_HEIGHT, 0.9375),
                Shapes.box(0.0625, 0.0, 0.3125, 0.6875, CAKE_HEIGHT, 0.9375)
            ),
            Shapes.box(0.0625, 0.0, 0.3125, 0.6875, CAKE_HEIGHT, 0.9375),
            Shapes.or(
                Shapes.box(0.0625, 0.0, 0.6875, 0.375, CAKE_HEIGHT, 0.9375),
                Shapes.box(0.0625, 0.0, 0.3125, 0.6875, CAKE_HEIGHT, 0.6875)
            ),
            Shapes.box(0.0625, 0.0, 0.3125, 0.6875, CAKE_HEIGHT, 0.6875),
            Shapes.box(0.3125, 0.0, 0.3125, 0.6875, CAKE_HEIGHT, 0.6875)
        )

        private val NORTH_SHAPES = createShapes(Direction.NORTH)
        private val WEST_SHAPES = createShapes(Direction.WEST)
        private val EAST_SHAPES = createShapes(Direction.EAST)

        private fun createShapes(to: Direction): List<VoxelShape> {
            return SOUTH_SHAPES.map { rotateShape(Direction.SOUTH, to, it) }
        }
    }

    init {
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(BITES, 0)
            .setValue(CANDLE, 0)
            .setValue(BlockStateProperties.LIT, false)
        )
    }

    override fun codec(): MapCodec<PokeSnackBlock> = RecordCodecBuilder.mapCodec { it.group(
        propertiesCodec(),
        PrimitiveCodec.BOOL.fieldOf("isLure").forGetter { it.isLure }
    ).apply(it, ::PokeSnackBlock) }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        builder.add(FACING, BITES, CANDLE, BlockStateProperties.LIT)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = PokeSnackBlockEntity(pos, state)

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape? {
        val snackShapes = when (state.getValue(FACING)) {
            Direction.SOUTH -> SOUTH_SHAPES
            Direction.WEST -> WEST_SHAPES
            Direction.EAST -> EAST_SHAPES
            else -> NORTH_SHAPES
        }

        var snackShape = snackShapes[state.getValue(BITES)]
        if (hasCandle(state)) {
            snackShape = Shapes.or(snackShape, CANDLE_SHAPE)
        }

        return snackShape
    }

    override fun getRenderShape(blockState: BlockState) = RenderShape.MODEL

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        var blockState = defaultBlockState()
        val worldView = ctx.level
        val blockPos = ctx.clickedPos
        ctx.nearestLookingDirections.forEach { direction ->
            if (direction.axis.isHorizontal) {
                blockState = blockState
                    .setValue(FACING, direction)
                        as BlockState
                if (blockState.canSurvive(worldView, blockPos)) {
                    return blockState
                }
            }
        }
        return null
    }

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        super.setPlacedBy(level, pos, state, placer, stack)

        val blockEntity = level.getBlockEntity(pos) as? PokeSnackBlockEntity ?: return

        blockEntity.initializeFromItemStack(stack)
        blockEntity.placedBy = placer?.uuid

        blockEntity.setChanged()
        level.sendBlockUpdated(pos, state, state, UPDATE_CLIENTS)
    }

    override fun getCloneItemStack(level: LevelReader, pos: BlockPos, state: BlockState): ItemStack {
        val blockEntity = level.getBlockEntity(pos) as? PokeSnackBlockEntity ?: return ItemStack.EMPTY
        return blockEntity.toItemStack()
    }

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
        if (!isLure) {
            if (!candleHit(hitResult)) {
                if (level.isClientSide) {
                    if (playerEat(level, pos, state, player).consumesAction()) {
                        return InteractionResult.SUCCESS
                    }

                    if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty) {
                        return InteractionResult.CONSUME
                    }
                }
                return playerEat(level, pos, state, player)
            } else if (canBeLit(state) && player.mainHandItem.isEmpty && player.isCrouching()) {
                player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack(getCandleById(state.getValue(CANDLE))))
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.4F + 0.8F)
                level.setBlockAndUpdate(pos, state.setValue(CANDLE, 0))
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos)
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult)
    }

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        if (!isLure) {
            if (stack.`is`(ItemTags.CANDLES) && !hasCandle(state)) {
                val item = stack.getItem()
                val candle = byItem(item)
                if (candle is CandleBlock) {
                    stack.consume(1, player)
                    level.playSound(null as Player?, pos, SoundEvents.CAKE_ADD_CANDLE, SoundSource.BLOCKS, 1.0F, 1.0F)
                    level.setBlockAndUpdate(pos, state.setValue(CANDLE, getIdByCandle(candle)))
                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos)
                    player.awardStat(Stats.ITEM_USED.get(item))
                    return ItemInteractionResult.SUCCESS
                }
            }

            if (candleHit(hitResult) && hasCandle(state)) {
                val isFlintAndSteel = stack.`is`(Items.FLINT_AND_STEEL)
                if (canBeLit(state) && (isFlintAndSteel || stack.`is`(Items.FIRE_CHARGE))) {
                    setCandleLit(level, state, pos, true)
                    level.playSound(
                        player,
                        pos,
                        if (isFlintAndSteel) SoundEvents.FLINTANDSTEEL_USE else SoundEvents.FIRECHARGE_USE,
                        SoundSource.BLOCKS,
                        1.0F,
                        level.random.nextFloat() * 0.4F + 0.8F
                    )

                    if (isFlintAndSteel) stack.hurtAndBreak(1, player, hand.toEquipmentSlot())
                    else stack.consume(1, player)

                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos)
                    return ItemInteractionResult.sidedSuccess(level.isClientSide)
                } else if (stack.isEmpty() && isCandleLit(state)) {
                    extinguishCandle(player, state, level, pos)
                    return ItemInteractionResult.sidedSuccess(level.isClientSide)
                }
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (isCandleLit(state)) {
            AbstractCandleBlock.addParticlesAndSound(
                level,
                Vec3(
                    pos.x + CANDLE_PARTICLE_POSITION.x,
                    pos.y + CANDLE_PARTICLE_POSITION.y,
                    pos.z + CANDLE_PARTICLE_POSITION.z
                ),
                random
            )
        }

        if (isLure && random.nextInt(5) == 0) {
            val tint = getTint(level, pos)
            val red = FastColor.ARGB32.red(tint) / 255F
            val green = FastColor.ARGB32.green(tint) / 255F
            val blue = FastColor.ARGB32.blue(tint) / 255F

            for (i in 0..<random.nextInt(1) + 1) {
                level.addParticle(
                    ColorParticleOption.create(
                        ParticleTypes.ENTITY_EFFECT,
                        red,
                        green,
                        blue
                    ),
                    pos.getX().toDouble() + Random.nextDouble(0.25, 0.76),
                    pos.getY().toDouble() + 0.4375,
                    pos.getZ().toDouble() + Random.nextDouble(0.25, 0.76),
                    0.0,
                    0.0,
                    0.0
                )
            }
        }
    }

    override fun onProjectileHit(level: Level, state: BlockState, hit: BlockHitResult, projectile: Projectile) {
        if (!level.isClientSide && projectile.isOnFire() && canBeLit(state)) {
            setCandleLit(level, state, hit.getBlockPos(), true)
        }
    }

    override fun playerWillDestroy(level: Level, blockPos: BlockPos, blockState: BlockState, player: Player): BlockState {
        dropCandle(level, blockPos, blockState, player)
        return super.playerWillDestroy(level, blockPos, blockState, player)
    }

    private fun dropCandle(level: Level, blockPos: BlockPos, blockState: BlockState, player: Player?) {
        if (hasCandle(blockState) && !level.isClientSide && (!(player?.isCreative ?: false))) {
            val itemEntity = ItemEntity(
                level,
                blockPos.x + 0.5,
                blockPos.y + CAKE_HEIGHT,
                blockPos.z + 0.5,
                ItemStack(getCandleById(blockState.getValue(CANDLE)))
            )
            itemEntity.setDefaultPickUpDelay()
            level.addFreshEntity(itemEntity)
        }
    }

    private fun playerEat(level: Level, pos: BlockPos, state: BlockState, player: Player): InteractionResult {
        if (!player.canEat(false)) {
            return InteractionResult.PASS
        } else {
            player.awardStat(Stats.EAT_CAKE_SLICE)
            player.getFoodData().eat(2, 0.1F)
            level.gameEvent(player, GameEvent.EAT, pos)
            eat(level, pos, state, player)
            return InteractionResult.SUCCESS
        }
    }

    fun eat(level: Level, pos: BlockPos, state: BlockState, player: Player?) {
        val bites = state.getValue(BITES) as Int
        val newBites =
            if (isLure) {
                val pokeSnackBlockEntity = level.getBlockEntity(pos) as PokeSnackBlockEntity? ?: return
                pokeSnackBlockEntity.amountSpawned += 1

                floor(pokeSnackBlockEntity.amountSpawned / PokeSnackBlockEntity.SPAWNS_PER_BITE.toDouble()).toInt()
            } else {
                bites + 1
            }

        if (newBites > MAX_BITES) {
            dropCandle(level, pos, state, player)

            level.removeBlock(pos, false)
            level.removeBlockEntity(pos)
        } else {
            level.setBlock(pos, state.setValue(BITES, newBites) as BlockState, UPDATE_ALL)
        }

        level.playSound(null, player?.blockPosition() ?: pos, SoundEvents.GENERIC_EAT, if (player != null) SoundSource.PLAYERS else SoundSource.NEUTRAL)
        spawnEatParticles(level, pos)

        player?.let {
            level.gameEvent(it, GameEvent.BLOCK_DESTROY, pos)
        }
    }

    private fun hasCandle(state: BlockState) = state.getValue(CANDLE) > 0
    private fun isCandleLit(state: BlockState) = state.getValue(BlockStateProperties.LIT)
    private fun canBeLit(state: BlockState) = hasCandle(state) && !isCandleLit(state)

    private fun setCandleLit(level: LevelAccessor, state: BlockState, pos: BlockPos, lit: Boolean) {
        level.setBlock(pos, state.setValue(BlockStateProperties.LIT, lit) as BlockState, UPDATE_ALL_IMMEDIATE)
    }

    private fun extinguishCandle(player: Player?, state: BlockState, level: LevelAccessor, pos: BlockPos) {
        setCandleLit(level, state, pos, false)
        level.addParticle(
            ParticleTypes.SMOKE,
            pos.getX().toDouble() + CANDLE_PARTICLE_POSITION.x(),
            pos.getY().toDouble() + CANDLE_PARTICLE_POSITION.y(),
            pos.getZ().toDouble() + CANDLE_PARTICLE_POSITION.z(),
            0.0,
            0.1,
            0.0
        )

        level.playSound(null as Player?, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F)
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos)
    }

    private fun candleHit(hit: BlockHitResult): Boolean {
        return hit.getLocation().y - hit.getBlockPos().getY().toDouble() > CAKE_HEIGHT
    }

    fun getTint(level: Level, pos: BlockPos): Int {
        val tint = (level.getBlockEntity(pos) as? PokeSnackBlockEntity)?.tint ?: 0xFFFFFF
        return if (tint == 0xFFFFFF) 0xF5EDA8 else tint
    }

    fun spawnEatParticles(level: Level, pos: BlockPos) {
        val random = level.random
        repeat(5) {
            level.addParticle(
                BlockParticleOption(ParticleTypes.BLOCK, CobblemonBlocks.POKE_SNACK.defaultBlockState()),
                pos.x + random.nextDouble(),
                pos.y + random.nextDouble() * CAKE_HEIGHT,
                pos.z + random.nextDouble(),
                0.0,
                0.0,
                0.0
            )
        }
    }

    override fun isRandomlyTicking(state: BlockState) = isLure

    override fun randomTick(
        state: BlockState,
        level: ServerLevel,
        pos: BlockPos,
        random: RandomSource
    ) {
        if (level.isClientSide) return
        (level.getBlockEntity(pos) as? PokeSnackBlockEntity)?.randomTick()
    }
}
