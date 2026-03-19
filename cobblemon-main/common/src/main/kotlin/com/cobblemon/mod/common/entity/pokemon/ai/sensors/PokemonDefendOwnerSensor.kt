/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon.ai.sensors

import com.cobblemon.mod.common.CobblemonMemories
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.getMemorySafely
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities
import net.minecraft.world.entity.ai.sensing.Sensor

class DefendOwnerSensor : Sensor<PokemonEntity>(10) {

    override fun requires() = setOf(CobblemonMemories.NEAREST_VISIBLE_ATTACKER)

    override fun doTick(world: ServerLevel, entity: PokemonEntity) {
        val owner = entity.owner ?: return
        entity.brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent { visibleMobs ->
            setNearestAttacker(entity, visibleMobs, owner)
        }
    }

    private fun setNearestAttacker(entity: PokemonEntity, visibleMobs: NearestVisibleLivingEntities, owner: LivingEntity) {
        val nearestAttacker = visibleMobs.findClosest { livingEntity ->
            // Prevents Pokémons from attacking players as owned Pokémons are currently invulnerable
            if (livingEntity is ServerPlayer) return@findClosest false

            if (livingEntity.lastHurtMob == owner) return@findClosest true
            if (livingEntity is Mob && livingEntity.target == owner) return@findClosest true

            val mobAttackTarget = livingEntity.brain.getMemorySafely(MemoryModuleType.ATTACK_TARGET)
            if (mobAttackTarget.isPresent && mobAttackTarget.get() == owner) return@findClosest true

            return@findClosest false
        }.map { mob -> mob as LivingEntity }

        entity.brain.setMemory(CobblemonMemories.NEAREST_VISIBLE_ATTACKER, nearestAttacker)
    }
}
