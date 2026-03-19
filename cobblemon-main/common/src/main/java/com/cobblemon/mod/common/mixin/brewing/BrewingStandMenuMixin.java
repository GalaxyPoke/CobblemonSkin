/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.brewing;

import com.cobblemon.mod.common.duck.RecipeAwareSlot;
import com.cobblemon.mod.common.item.crafting.brewingstand.BrewingStandRecipe;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandMenu.class)
public abstract class BrewingStandMenuMixin {
	@Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V", at = @At("TAIL"))
	private void captureLevel(int containerId, Inventory playerInventory, Container brewingStandContainer, ContainerData brewingStandData, CallbackInfo ci) {
		var recipeManager = playerInventory.player.level().getRecipeManager();
		for (Slot slot : ((BrewingStandMenu) (Object) this).slots) {
			if (slot instanceof RecipeAwareSlot awareSlot) {
				awareSlot.setRecipeManager(recipeManager);
			}
		}
	}
}
