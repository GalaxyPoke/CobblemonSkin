/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common


import com.cobblemon.mod.common.item.crafting.CookingPotRecipe
import com.cobblemon.mod.common.item.crafting.CookingPotShapelessRecipe
import com.cobblemon.mod.common.item.crafting.brewingstand.BrewingStandRecipe
import com.cobblemon.mod.common.platform.PlatformRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer

object CobblemonRecipeSerializers : PlatformRegistry<Registry<RecipeSerializer<*>>, ResourceKey<Registry<RecipeSerializer<*>>>, RecipeSerializer<*>>() {

    val COOKING_POT_COOKING: RecipeSerializer<*> = register("cobblemon:cooking_pot", CookingPotRecipe.Serializer())
    val COOKING_POT_SHAPELESS: RecipeSerializer<*> = register("cobblemon:cooking_pot_shapeless", CookingPotShapelessRecipe.Serializer())

    val BREWING_STAND: RecipeSerializer<*> = register("cobblemon:brewing_stand", BrewingStandRecipe.Serializer())
    
    override val registry: Registry<RecipeSerializer<*>>
        get() = BuiltInRegistries.RECIPE_SERIALIZER
    override val resourceKey: ResourceKey<Registry<RecipeSerializer<*>>>
        get() = Registries.RECIPE_SERIALIZER

    fun <S : RecipeSerializer<T>, T : Recipe<*>> register(key: String, recipeSerializer: S): S {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, key, recipeSerializer) as S
    }
}