/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.crafting

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.api.fishing.SpawnBait
import com.cobblemon.mod.common.api.fishing.SpawnBaitEffects
import com.cobblemon.mod.common.item.components.BaitEffectsComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

object BaitSeasoningProcessor : SeasoningProcessor {
    override val type = "spawn_bait"
    override fun apply(result: ItemStack, seasoning: List<ItemStack>) {
        val baitIdentifiers = mutableListOf<ResourceLocation>()
        for (seasoningStack in seasoning) {
            baitIdentifiers.addAll(SpawnBaitEffects.getBaitIdentifiersFromItem(seasoningStack.itemHolder))

            // From seasonings with baitEffects
            val seasoningData = Seasonings.getFromItemStack(seasoningStack)
            if (seasoningData != null && !seasoningData.baitEffects.isNullOrEmpty()) {
                val itemId = seasoningStack.itemHolder.unwrapKey().get().location()
                val id = ResourceLocation("seasonings", itemId.path)
                baitIdentifiers.add(id)
            }
        }
        result.set(CobblemonItemComponents.BAIT_EFFECTS, BaitEffectsComponent(baitIdentifiers.toList()))
    }

    override fun consumesItem(seasoning: ItemStack): Boolean {
        val seasoningData = Seasonings.getFromItemStack(seasoning)
        return seasoningData != null && (!seasoningData.baitEffects.isNullOrEmpty() || SpawnBaitEffects.getBaitIdentifiersFromItem(seasoning.itemHolder).isNotEmpty())
    }
}