/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.integration.jei.cooking

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.cobblemon.mod.common.integration.jei.CobblemonJeiProvider
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.registration.IRecipeCategoryRegistration
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.client.Minecraft

class CampfirePotJeiProvider : CobblemonJeiProvider {
    override fun registerCategory(registration: IRecipeCategoryRegistration) {
        registration.addRecipeCategories(CampfirePotRecipeCategory(registration))
    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        val recipeManger = Minecraft.getInstance().level?.recipeManager ?: throw IllegalStateException("Recipe manager not found")

        val shapelessRecipes = recipeManger.getAllRecipesFor(CobblemonRecipeTypes.COOKING_POT_SHAPELESS).map { it.value }
        val cookingRecipes = recipeManger.getAllRecipesFor(CobblemonRecipeTypes.COOKING_POT_COOKING).map { it.value }

        registration.addRecipes(CampfirePotRecipeCategory.RECIPE_TYPE, shapelessRecipes)
        registration.addRecipes(CampfirePotRecipeCategory.RECIPE_TYPE, cookingRecipes)
    }

    override fun registerRecipeCatalsysts(registration: IRecipeCatalystRegistration) {
        CobblemonItems.campfire_pots.forEach {
            registration.addRecipeCatalyst(it, CampfirePotRecipeCategory.RECIPE_TYPE)
        }
    }
}