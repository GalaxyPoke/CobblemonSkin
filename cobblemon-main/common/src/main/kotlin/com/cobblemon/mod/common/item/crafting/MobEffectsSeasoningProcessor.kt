/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.crafting

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.cooking.MobEffectUtils
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.api.cooking.SerializableMobEffectInstance
import com.cobblemon.mod.common.item.components.MobEffectsComponent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.effect.MobEffectInstance

object MobEffectSeasoningProcessor : SeasoningProcessor {
    override val type = "mob_effects"

    override fun apply(result: ItemStack, seasoning: List<ItemStack>) {
        val allEffects: List<SerializableMobEffectInstance> = seasoning
                .flatMap { Seasonings.getMobEffectsFromItemStack(it) }

        val mergedEffects = MobEffectUtils.mergeEffects(allEffects).map { it.toInstance() }

        if (mergedEffects.isNotEmpty()) {
            result.set(CobblemonItemComponents.MOB_EFFECTS, MobEffectsComponent(mergedEffects))
        }
    }

    override fun consumesItem(seasoning: ItemStack): Boolean {
        val seasoningData = Seasonings.getFromItemStack(seasoning)
        return seasoningData?.mobEffects != null
    }
}