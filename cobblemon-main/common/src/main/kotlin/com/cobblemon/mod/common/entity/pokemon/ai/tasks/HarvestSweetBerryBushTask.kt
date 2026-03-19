/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.getMemorySafely
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CaveVines
import net.minecraft.world.level.block.SweetBerryBushBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent


class HarvestSweetBerryBushTask : Behavior<LivingEntity>(
        mapOf(
                MemoryModuleType.WALK_TARGET to MemoryStatus.VALUE_ABSENT,
                CobblemonMemories.NEARBY_SWEET_BERRY_BUSH to MemoryStatus.VALUE_PRESENT
        )
) {

    var startTime : Int = 0
    companion object {
        const val WAIT_TIME = 40
    }

    override fun canStillUse(world: ServerLevel, entity: LivingEntity, l: Long): Boolean {
        if (startTime > WAIT_TIME) {
            return false
        }
        val blockPos = entity.brain.getMemorySafely(CobblemonMemories.NEARBY_SWEET_BERRY_BUSH).orElse(null)
        if (blockPos == null) return false
        val blockState = world.getBlockState(blockPos)
        return (blockState.block == Blocks.SWEET_BERRY_BUSH && blockState.getValue(SweetBerryBushBlock.AGE) >= 2)
                || CaveVines.hasGlowBerries(blockState)
    }

    override fun checkExtraStartConditions(world: ServerLevel, entity: LivingEntity): Boolean {

        val blockPos = entity.brain.getMemory(CobblemonMemories.NEARBY_SWEET_BERRY_BUSH).get()

        val entityPos = entity.blockPosition()

        if (!entityPos.equals(blockPos)) {
            return false
        }

        val blockState = world.getBlockState(blockPos)
        if (blockState.`is`(Blocks.SWEET_BERRY_BUSH) && blockState.getValue(SweetBerryBushBlock.AGE) >= 2) {
            return true
        } else {
            return CaveVines.hasGlowBerries(blockState)
        }
    }

    override fun tick(level: ServerLevel, entity: LivingEntity, gameTime: Long) {
        startTime += 1
        if (startTime == WAIT_TIME) {
            entity as PokemonEntity
            val world = entity.level()
            if (!world.gameRules.getRule(GameRules.RULE_MOBGRIEFING).get()) {
                return
            }
            val blockPos = entity.brain.getMemory(CobblemonMemories.NEARBY_SWEET_BERRY_BUSH).get()
            val blockState = world.getBlockState(blockPos)
            if (CaveVines.hasGlowBerries(blockState)) { // Glow Berries Case
                CaveVines.use(entity, blockState, world, blockPos)
            } else if (blockState.`is`(Blocks.SWEET_BERRY_BUSH)) { // Sweet Berries Case
                blockState.setValue(SweetBerryBushBlock.AGE, 1)
                var j: Int = 1 + entity.level().random.nextInt(2) + if (blockState.getValue<Int>(SweetBerryBushBlock.AGE) == 3) 1 else 0
                val itemStack: ItemStack = entity.pokemon.heldItem
                if (itemStack.isEmpty) {
                    entity.pokemon.swapHeldItem(ItemStack(Items.SWEET_BERRIES))
                    --j
                }
                if (j > 0) {
                    Block.popResource(entity.level(), blockPos, ItemStack(Items.SWEET_BERRIES, j))
                }
                entity.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0f, 1.0f)
                entity.level().setBlock(blockPos, blockState.setValue(SweetBerryBushBlock.AGE, 1) as BlockState, 2)
                entity.level().gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity))
            }
            entity.brain.eraseMemory(CobblemonMemories.TIME_TRYING_TO_REACH_BERRY_BUSH)
            entity.brain.eraseMemory(CobblemonMemories.NEARBY_SWEET_BERRY_BUSH)
        }
    }

    override fun start(world: ServerLevel, entity: LivingEntity, l: Long) {
        startTime = 0
    }
}
