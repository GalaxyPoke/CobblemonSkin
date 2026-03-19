/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.tasks

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.DataKeys
import com.google.common.collect.ImmutableMap
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate

/**
 * The baa's eat the graa's
 *
 * @author Hiroku
 * @since April 6th, 2024
 */
class EatGrassTask(
    val eatingChance: Float,
    val cooldownTicks: Long
) : Behavior<PokemonEntity>(
    ImmutableMap.of(
        MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
        CobblemonMemories.RECENTLY_ATE_GRASS, MemoryStatus.VALUE_ABSENT
    )
) {
    val grassPredicate = BlockStatePredicate.forBlock(Blocks.GRASS_BLOCK)
    var timer = -1

    override fun checkExtraStartConditions(world: ServerLevel, entity: PokemonEntity): Boolean {
        if (world.random.nextFloat() > eatingChance) {
            return false
        } else {
            entity.pokemon.getFeature<FlagSpeciesFeature>(DataKeys.HAS_BEEN_SHEARED)?.enabled?.takeIf { it } ?: run {
                return false
            }
            val blockPos = entity.blockPosition()
            if (grassPredicate.test(world.getBlockState(blockPos)) ) {
                return true
            } else if (world.getBlockState(blockPos.below()).`is`(Blocks.GRASS_BLOCK)) {
                return true
            }
            return false
        }
    }

    override fun start(world: ServerLevel, entity: PokemonEntity, time: Long) {
        timer = 40
        entity.brain.setMemoryWithExpiry(CobblemonMemories.PATH_COOLDOWN, true, 40L)
        entity.playAnimation("eat")
        world.broadcastEntityEvent(entity, 10.toByte())
    }

    override fun canStillUse(world: ServerLevel, entity: PokemonEntity, time: Long): Boolean {
        timer--
        if (timer < 0) {
            val blockPos = entity.blockPosition()
            if (grassPredicate.test(world.getBlockState(blockPos)) ) {
                if (world.gameRules.getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    world.destroyBlock(blockPos, false)
                }
                entity.ate()
                entity.brain.setMemoryWithExpiry(CobblemonMemories.RECENTLY_ATE_GRASS, true, cooldownTicks)
            } else if (world.getBlockState(blockPos.below()).`is`(Blocks.GRASS_BLOCK)) {
                val blockPos2 = blockPos.below()
                if (world.getBlockState(blockPos2).`is`(Blocks.GRASS_BLOCK)) {
                    if (world.gameRules.getBoolean(GameRules.RULE_MOBGRIEFING)) {
                        world.levelEvent(2001, blockPos2, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()))
                        world.setBlock(blockPos2, Blocks.DIRT.defaultBlockState(), 2)
                    }

                    entity.brain.setMemoryWithExpiry(CobblemonMemories.RECENTLY_ATE_GRASS, true, cooldownTicks)
                    entity.ate()
                }
            }
            return false
        }
        return true
    }
}