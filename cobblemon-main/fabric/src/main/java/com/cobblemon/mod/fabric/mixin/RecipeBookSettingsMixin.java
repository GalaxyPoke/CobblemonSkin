/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.mixin;

import com.cobblemon.mod.common.CobblemonRecipeBookTypes;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Map;

@Mixin(RecipeBookSettings.class)
public class RecipeBookSettingsMixin {

    @Shadow
    @Final
    public Map<RecipeBookType, RecipeBookSettings.TypeSettings> states;

    @Inject(method = "<init>(Ljava/util/Map;)V", at = @At("TAIL"))
    private void cobblemon$init(CallbackInfo ci) {
        // if you change or add any of these, neoforge uses a separate mechanism
        // check enumextensions.json and `CobblemonEnumExtensions`
        states.put(CobblemonRecipeBookTypes.INSTANCE.getCOOKING_POT(), new RecipeBookSettings.TypeSettings(true, true));
    }

    // this fixes a disconnect issue with vanilla servers where the network sync relies on the enum we modify in a common mixin to be the same on both sides
    @Redirect( method = "read(Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/stats/RecipeBookSettings;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/RecipeBookType;values()[Lnet/minecraft/world/inventory/RecipeBookType;"))
    private static RecipeBookType[] cobblemon$valuesReadRedirect() {
        return Arrays.stream(RecipeBookType.values()).filter( recipeBookType -> !CobblemonRecipeBookTypes.INSTANCE.getCOOKING_POT().equals(recipeBookType) ).toArray(RecipeBookType[]::new);
    }

    @Redirect( method = "write(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/RecipeBookType;values()[Lnet/minecraft/world/inventory/RecipeBookType;"))
    private RecipeBookType[] cobblemon$valuesWriteRedirect() {
        return Arrays.stream(RecipeBookType.values()).filter( recipeBookType -> !CobblemonRecipeBookTypes.INSTANCE.getCOOKING_POT().equals(recipeBookType) ).toArray(RecipeBookType[]::new);
    }
}
