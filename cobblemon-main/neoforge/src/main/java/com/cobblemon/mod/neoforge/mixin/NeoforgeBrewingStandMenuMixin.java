/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge.mixin;

import com.cobblemon.mod.common.item.crafting.brewingstand.BrewingStandRecipe;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BrewingStandMenu.class)
public abstract class NeoforgeBrewingStandMenuMixin {
    @WrapOperation(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/BrewingStandMenu$PotionSlot;mayPlaceItem(Lnet/minecraft/world/item/alchemy/PotionBrewing;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean cobblemon$isPotionBottle(PotionBrewing potionBrewing, ItemStack itemStack, Operation<Boolean> original, @Local(argsOnly = true) @NotNull Player player) {
        return BrewingStandRecipe.Companion.isBottle(itemStack, player.level().getRecipeManager()) || original.call(potionBrewing, itemStack);
    }
}
