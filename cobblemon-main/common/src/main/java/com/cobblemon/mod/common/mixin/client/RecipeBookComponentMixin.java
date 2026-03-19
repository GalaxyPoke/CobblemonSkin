/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.CobblemonRecipeCategories;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeBookComponent.class)
public class RecipeBookComponentMixin {

    @Shadow private ClientRecipeBook book;

    @Shadow protected Minecraft minecraft;

    @Shadow private int width;

    @Shadow private int xOffset;

    @Shadow private int height;

    @Shadow @Final private RecipeBookPage recipeBookPage;

    @Inject(method = "updateTabs", at = @At("HEAD"), cancellable = true)
    private void customUpdateTabs(CallbackInfo ci) {
        RecipeBookComponent instance = (RecipeBookComponent) (Object) this;
        int i = (width - 147) / 2 - xOffset - 30;
        int j = (height - 166) / 2 + 3;
        int k = 27;
        int l = 0;

        for (RecipeBookTabButton recipeBookTabButton : instance.tabButtons) {
            RecipeBookCategories recipeBookCategories = recipeBookTabButton.getCategory();
            if (recipeBookCategories != RecipeBookCategories.CRAFTING_SEARCH && recipeBookCategories != RecipeBookCategories.FURNACE_SEARCH && recipeBookCategories != CobblemonRecipeCategories.COOKING_POT_SEARCH.toVanillaCategory()) {
                if (recipeBookTabButton.updateVisibility(book)) {
                    recipeBookTabButton.setPosition(i, j + 27 * l++);
                    recipeBookTabButton.startAnimation(minecraft);
                }
            } else {
                recipeBookTabButton.visible = true;
                recipeBookTabButton.setPosition(i, j + k * l++);
            }
        }

        ci.cancel();
    }
}