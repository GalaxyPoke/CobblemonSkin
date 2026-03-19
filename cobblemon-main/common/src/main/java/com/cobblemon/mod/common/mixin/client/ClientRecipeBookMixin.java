/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.CobblemonRecipeCategories;
import com.cobblemon.mod.common.item.crafting.CookingPotBookCategory;
import com.cobblemon.mod.common.item.crafting.CookingPotRecipe;
import com.cobblemon.mod.common.item.crafting.CookingPotRecipeBase;
import com.cobblemon.mod.common.item.crafting.CookingPotShapelessRecipe;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(ClientRecipeBook.class)
public abstract class ClientRecipeBookMixin {

    @Shadow
    protected static Map<RecipeBookCategories, List<List<RecipeHolder<?>>>> categorizeAndGroupRecipes(Iterable<RecipeHolder<?>> recipes) {
        return null;
    }

    @Shadow
    private Map<RecipeBookCategories, List<RecipeCollection>> collectionsByTab;

    @Shadow
    private List<RecipeCollection> allCollections;

    @Inject(method = "getCategory", at = @At(value = "HEAD"), cancellable = true)
    private static void addCustomCategory(RecipeHolder<?> recipe, CallbackInfoReturnable<RecipeBookCategories> cir) {
        Recipe<?> recipe2 = recipe.value();
        if (recipe2 instanceof CookingPotRecipeBase) {
            CookingPotBookCategory category = ((CookingPotRecipeBase) recipe2).getCategory();
            RecipeBookCategories var7;
            switch (category) {
                case MISC -> var7 = CobblemonRecipeCategories.COOKING_POT_MISC.toVanillaCategory();
                case FOODS -> var7 = CobblemonRecipeCategories.COOKING_POT_FOODS.toVanillaCategory();
                case MEDICINES -> var7 = CobblemonRecipeCategories.COOKING_POT_MEDICINES.toVanillaCategory();
                case COMPLEX_DISHES -> var7 = CobblemonRecipeCategories.COOKING_POT_COMPLEX_DISHES.toVanillaCategory();
                default -> throw new MatchException(null, null);
            }
            cir.setReturnValue(var7);
        }
    }

    @Inject(method = "setupCollections", at = @At("RETURN"))
    private void addCustomAggregateCategories(Iterable<RecipeHolder<?>> recipes, RegistryAccess registryAccess, CallbackInfo ci) {
        Map<RecipeBookCategories, List<List<RecipeHolder<?>>>> map = categorizeAndGroupRecipes(recipes);
        Map<RecipeBookCategories, List<RecipeCollection>> map2 = Maps.newHashMap();
        ImmutableList.Builder<RecipeCollection> builder = ImmutableList.builder();

        map.forEach((recipeBookCategories, list) -> {
            List<RecipeCollection> collections = list.stream()
                    .map(listx -> new RecipeCollection(registryAccess, listx))
                    .peek(builder::add)
                    .collect(ImmutableList.toImmutableList());
            map2.put(recipeBookCategories, collections);
        });
        RecipeBookCategories.AGGREGATE_CATEGORIES.forEach((recipeBookCategories, list) -> map2.put(recipeBookCategories, (List) list.stream().flatMap((recipeBookCategoriesx) -> ((List) map2.getOrDefault(recipeBookCategoriesx, ImmutableList.of())).stream()).collect(ImmutableList.toImmutableList())));
        CobblemonRecipeCategories.Companion.getCustomAggregateCategories().forEach((recipeBookCategories, list) -> map2.put(recipeBookCategories, (List) list.stream().flatMap((recipeBookCategoriesx) -> ((List) map2.getOrDefault(recipeBookCategoriesx, ImmutableList.of())).stream()).collect(ImmutableList.toImmutableList())));

        this.collectionsByTab = ImmutableMap.copyOf(map2);
        this.allCollections = builder.build();
    }
}