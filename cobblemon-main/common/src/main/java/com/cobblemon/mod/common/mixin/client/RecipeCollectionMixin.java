/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@Mixin(RecipeCollection.class)
public class RecipeCollectionMixin {

    @Shadow @Final private Set<RecipeHolder<?>> fitsDimensions;

    @Shadow @Final private Set<RecipeHolder<?>> craftable;

    @Shadow @Final private List<RecipeHolder<?>> recipes;

    @Inject(method = "canCraft", at = @At("HEAD"), cancellable = true)
    private void customCanCraft(StackedContents handler, int width, int height, RecipeBook book, CallbackInfo ci) {
        for (RecipeHolder<?> recipeHolder : recipes) {
            boolean bl = recipeHolder.value().canCraftInDimensions(width, height) && book.contains(recipeHolder);
            if (bl) {
                fitsDimensions.add(recipeHolder);
            } else {
                fitsDimensions.remove(recipeHolder);
            }

            if (bl && handler.canCraft(recipeHolder.value(), null)) {
                craftable.add(recipeHolder);
            } else {
                craftable.remove(recipeHolder);
            }
        }

        ci.cancel();
    }
}