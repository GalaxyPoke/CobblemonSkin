/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.mixin;


import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.CobblemonRecipeBookTypes;
import com.cobblemon.mod.common.CobblemonRecipeCategories;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;


@Mixin(RecipeBookCategories.class)
public class RecipeBookCategoriesMixin {

    @Final
    @Mutable
    @Shadow
    private static RecipeBookCategories[] $VALUES;

    @Invoker("<init>")
    private static RecipeBookCategories cobblemon$createCategory(String name, int ordinal, ItemStack... icons) {
        throw new AssertionError();
    }

    @Inject(
            method = "<clinit>",
            at = @At("TAIL")
    )
    private static void cobblemon$addRecipeBookType(CallbackInfo ci) {
        ArrayList<RecipeBookCategories> types = new ArrayList<>(List.of($VALUES));
        // if you change or add any of these, neoforge uses a separate mechanism
        // check enumextensions.json and `CobblemonEnumExtensions`
        types.add(cobblemon$createCategory("COBBLEMON_COOKING_POT_SEARCH", $VALUES.length, new ItemStack(Items.COMPASS)));
        types.add(cobblemon$createCategory("COBBLEMON_COOKING_POT_FOODS", $VALUES.length + 1, new ItemStack(CobblemonItems.LEEK_AND_POTATO_STEW)));
        types.add(cobblemon$createCategory("COBBLEMON_COOKING_POT_MEDICINES", $VALUES.length + 2, new ItemStack(CobblemonItems.POTION)));
        types.add(cobblemon$createCategory("COBBLEMON_COOKING_POT_COMPLEX_DISHES", $VALUES.length + 3, new ItemStack(CobblemonItems.APRIJUICE_RED)));
        types.add(cobblemon$createCategory("COBBLEMON_COOKING_POT_MISC", $VALUES.length + 4, new ItemStack(CobblemonItems.PROTEIN)));
        $VALUES = types.toArray(RecipeBookCategories[]::new);
    }

    @Inject(method = "getCategories", at = @At("HEAD"), cancellable = true)
    private static void modifyCategories(RecipeBookType recipeBookType, CallbackInfoReturnable<List<RecipeBookCategories>> cir) {
        if (recipeBookType == CobblemonRecipeBookTypes.INSTANCE.getCOOKING_POT()) {
            List var10000 = ImmutableList.of(
                    CobblemonRecipeCategories.COOKING_POT_SEARCH.toVanillaCategory(),
                    CobblemonRecipeCategories.COOKING_POT_FOODS.toVanillaCategory(),
                    CobblemonRecipeCategories.COOKING_POT_MEDICINES.toVanillaCategory(),
                    CobblemonRecipeCategories.COOKING_POT_COMPLEX_DISHES.toVanillaCategory(),
                    CobblemonRecipeCategories.COOKING_POT_MISC.toVanillaCategory());
            cir.setReturnValue(var10000);
            return;
        }
    }
}
