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
import com.cobblemon.mod.common.pokemon.ai.ObtainableItem
import com.cobblemon.mod.common.util.findMatchingEntry
import com.cobblemon.mod.common.util.getMemorySafely
import com.cobblemon.mod.common.util.getObjectList
import com.google.common.collect.ImmutableSet
import java.util.Optional
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.Sensor
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.GameRules

class PokemonItemSensor(
    private val width: Double = 16.0, // TODO: Can we configure sensors dynamically? // Nope. Could be done with a config struct option but it's a bit crass.
    private val height: Double = 8.0,
    private val maxTravelDistance: Double = 16.0,
) : Sensor<PokemonEntity>(30) {
    companion object {
        const val PICKUP_ITEMS = "pickup_items"
    }

    override fun requires(): MutableSet<MemoryModuleType<*>?> {
        return ImmutableSet.of<MemoryModuleType<*>?>(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)
    }

    override fun doTick(level: ServerLevel, entity: PokemonEntity) {
        val pickupItems = entity.config.getObjectList<ObtainableItem>(PICKUP_ITEMS)
        if (!level.gameRules.getBoolean(GameRules.RULE_MOBGRIEFING) || !entity.pokemon.canDropHeldItem || entity.brain.getMemorySafely(
                CobblemonMemories.DISABLE_WALK_TO_WANTED_ITEM).orElse(false)) {
            // Mob griefing is disabled, the Pokemon cannot swap out its item, or it's exhausted from attempting to go to an unreachable item
            // so don't bother to search
            return
        }

        val heldItemValue = pickupItems.findMatchingEntry(entity.registryAccess(), entity.pokemon.heldItem())?.pickupPriority ?: 0
        if (heldItemValue >= (pickupItems.maxOfOrNull { it.pickupPriority } ?: 0)) {
            // It's already holding the highest value item it can have, no need to look for better.
            return
        }

        val list = level.getEntitiesOfClass(
            ItemEntity::class.java,
            entity.boundingBox.inflate(width, height, width)
        ) {
            (pickupItems.findMatchingEntry(entity.registryAccess(), it.item)?.pickupPriority ?: 0) > heldItemValue
                    && it.closerThan(entity, maxTravelDistance)
                    && entity.hasLineOfSight(it)
        }

        // Find the closest item to the entity
        val nearestItemEntity = list.minByOrNull { it.distanceTo(entity) }
        entity.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, Optional.ofNullable(nearestItemEntity))
    }

}