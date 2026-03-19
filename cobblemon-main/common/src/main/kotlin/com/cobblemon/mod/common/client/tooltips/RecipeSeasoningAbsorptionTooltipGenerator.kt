/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.tooltips

import com.cobblemon.mod.common.CobblemonItemComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

object RecipeSeasoningAbsorptionTooltipGenerator : TooltipGenerator() {
    override fun generateCategoryTooltip(stack: ItemStack, lines: MutableList<Component>): MutableList<Component>? {
        // Skip if this is a crafting result preview. Vera wanted to be able to give extra context to things not fully crafted for added guidance in crafting UIs
        if (stack.get(CobblemonItemComponents.CRAFTED) == true) {
            return mutableListOf()
        }

        if (itemTakesSeasoningData(stack)) {
            val accepted = mutableListOf<Component>()

            if (recipeUsesFlavor(stack)) accepted.add(flavorHeader)
            if (recipeUsesFood(stack)) accepted.add(foodHeader)
            if (recipeUsesMobEffect(stack)) accepted.add(mobEffectHeader)
            if (recipeUsesBaitEffect(stack)) accepted.add(baitEffectHeader)

            if (accepted.isNotEmpty()) {
                return mutableListOf(generateSeasoningAbsorbtionTooltip(accepted))
            }
        }

        return null
    }
}