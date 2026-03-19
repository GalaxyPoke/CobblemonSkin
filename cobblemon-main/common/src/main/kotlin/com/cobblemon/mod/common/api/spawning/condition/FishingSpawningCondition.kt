/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.position.FishingSpawnablePosition
import com.cobblemon.mod.common.util.itemRegistry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Block

/**
 * A spawning condition that applies to [FishingSpawnablePosition]s.
 *
 * @author Hiroku
 * @since February 3rd, 2024
 */
class FishingSpawningCondition: SpawningCondition<FishingSpawnablePosition>() {
    override fun spawnablePositionClass() = FishingSpawnablePosition::class.java

    var rod: RegistryLikeCondition<Item>? = null
    var neededNearbyBlocks: MutableList<RegistryLikeCondition<Block>>? = null
    var minLureLevel: Int? = null
    var maxLureLevel: Int? = null
    var bait: ResourceLocation? = null
    var rodType: ResourceLocation? = null

    override fun fits(spawnablePosition: FishingSpawnablePosition): Boolean {
        if (!super.fits(spawnablePosition)) {
            return false
        } else if (rod != null && !rod!!.fits(spawnablePosition.rodItem ?: return false, spawnablePosition.world.itemRegistry)) {
            return false
        } else if (neededNearbyBlocks != null && neededNearbyBlocks!!.none { cond -> spawnablePosition.nearbyBlockTypes.any { cond.fits(it, spawnablePosition.blockRegistry) } }) {
            return false
        }

        if (minLureLevel != null) { // check for the lureLevel of the rod
            val pokerodStack = spawnablePosition.rodStack
            val lureLevel = spawnablePosition.enchantmentRegistry.getHolder(Enchantments.LURE).map {
                EnchantmentHelper.getItemEnchantmentLevel(
                    it,
                    pokerodStack
                )
            }.orElse(0)
            if (lureLevel < minLureLevel!!) {
                return false
            } else if (maxLureLevel != null && lureLevel > maxLureLevel!!) {
                return false
            }
        }
        if (bait != null) { // check for the bait on the bobber
            val pokerodBait = spawnablePosition.baitStack.itemHolder.unwrapKey().orElse(null)?.location()
            if (pokerodBait != bait) {
                return false
            }
        }
        if (rodType != null) { // check for the type of pokerod being used
            val pokerodItem = spawnablePosition.rodItem
            if (pokerodItem?.pokeRodId != rodType) {
                return false
            }
        }
        return true
    }

    companion object {
        const val NAME = "fishing"
    }
}