/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.ai.CobblemonAttackTargetData
import com.cobblemon.mod.common.api.ai.CobblemonWanderControl
import com.cobblemon.mod.common.api.dialogue.ActiveDialogue
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.ListCodec
import com.mojang.serialization.codecs.PrimitiveCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.core.UUIDUtil
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import java.util.*

object CobblemonMemories {
    val memories = mutableMapOf<String, MemoryModuleType<*>>()

    /*
     * When a memory doesn't have a codec provided, it's because it doesn't
     * get serialized during relogs. This makes sense for things like battles which
     * do not survive a relog therefore any memory of it should be temporary.
     */
    @JvmField
    val BATTLING_POKEMON = register("battling_pokemon", ListCodec(UUIDUtil.CODEC, 0, 31))
    @JvmField
    val NPC_BATTLING = register("npc_battling", PrimitiveCodec.BOOL)
    @JvmField
    val DIALOGUES = register<List<ActiveDialogue>>("npc_dialogues")
    @JvmField
    val ACTIVE_ACTION_EFFECT = register<ActionEffectContext>("active_action_effect")
    @JvmField
    val POKEMON_FLYING = register("pokemon_flying", PrimitiveCodec.BOOL)
    @JvmField
    val POKEMON_DROWSY = register("pokemon_drowsy", PrimitiveCodec.BOOL)
    @JvmField
    val POKEMON_SLEEPING = register("pokemon_sleeping", PrimitiveCodec.BOOL)
    @JvmField
    val POKEMON_BATTLE = register<UUID>("pokemon_battle")
    @JvmField
    val PATH_COOLDOWN = register<Boolean>("path_cooldown")
    @JvmField
    val TARGETED_BATTLE_POKEMON = register<UUID>("targeted_battle_pokemon")
    @JvmField
    val NEAREST_VISIBLE_ATTACKER = register<LivingEntity>("nearest_visible_attacker")
    @JvmField
    val NEARBY_GROWABLE_CROPS = register<BlockPos>("nearby_growable_crops")
    @JvmField
    val NEARBY_SWEET_BERRY_BUSH = register<BlockPos>("nearest_sweet_berry_bush")
    @JvmField
    val DISABLE_WALK_TO_BERRY_BUSH = register<Boolean>("disable_walk_to_berry_bush")
    @JvmField
    val TIME_TRYING_TO_REACH_BERRY_BUSH = register<Int>("time_trying_to_reach_berry_bush")
    @JvmField
    val IS_CONSUMING_ITEM = register<Boolean>("is_consuming_item")
    @JvmField
    val RECENTLY_ATE_GRASS = register<Boolean>("recently_ate_grass")
    @JvmField
    val HIVE_LOCATION = register<BlockPos>("hive_location", BlockPos.CODEC)
    @JvmField
    val HIVE_BLACKLIST = register<List<BlockPos>>("hive_blacklist")
    @JvmField
    val HIVE_COOLDOWN = register<Boolean>("hive_cooldown")
    @JvmField
    val NEARBY_FLOWER = register<BlockPos>("nearby_flower", BlockPos.CODEC)
    @JvmField
    val PATH_TO_NEARBY_FLOWER_COOLDOWN = register<Boolean>("path_to_nearby_flower_cooldown")
    @JvmField
    val HAS_NECTAR = register<Boolean>("has_nectar", PrimitiveCodec.BOOL)
    @JvmField
    val NEARBY_SACC_LEAVES = register<BlockPos>("nearby_sacc_leaves", BlockPos.CODEC)
    /** who am i following rn? */
    @JvmField
    val HERD_LEADER = register<String>("herd_leader", PrimitiveCodec.STRING)
    /** how many are following me rn? */
    @JvmField
    val HERD_SIZE = register<Int>("herd_size") // Don't bother saving it, we'll try to keep count roughly.
    @JvmField
    val ATTACK_TARGET_DATA = register<CobblemonAttackTargetData>("attack_target_data") // This is used to store additional information about the attack target, such as when to give up pursuit.
    @JvmField
    val WANDER_CONTROL = register<CobblemonWanderControl>("wander_control")
    @JvmField
    val DISABLE_WALK_TO_WANTED_ITEM = register<Boolean>("disable_walk_to_wanted_item")
    @JvmField
    val TIME_TRYING_TO_REACH_WANTED_ITEM = register<Int>("time_trying_to_wanted_item")

    @JvmStatic
    fun <U> register(id: String, codec: Codec<U>): MemoryModuleType<U> {
        val memoryModule = MemoryModuleType(Optional.of(codec))
        memories[id] = memoryModule
        return memoryModule
    }

    @JvmStatic
    fun <U> register(id: String): MemoryModuleType<U> {
        val memoryModule = MemoryModuleType<U>(Optional.empty())
        memories[id] = memoryModule
        return memoryModule
    }
}