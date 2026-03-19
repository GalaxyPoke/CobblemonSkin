/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.integration.jei.brewing

import com.cobblemon.mod.common.item.crafting.brewingstand.BrewingStandRecipe
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

class JeiBrewingStandRecipe(
    private val recipe: BrewingStandRecipe,
    private val id: ResourceLocation
) : IJeiBrewingRecipe {

    override fun getPotionInputs(): List<ItemStack> {
        return recipe.bottle.items.asList()
    }

    override fun getIngredients(): List<ItemStack> {
        return recipe.input.items.asList()
    }

    override fun getPotionOutput(): ItemStack {
        return recipe.result.copy()
    }

    override fun getBrewingSteps(): Int {
        return 1
    }

    override fun getUid(): ResourceLocation {
        return id
    }
}
