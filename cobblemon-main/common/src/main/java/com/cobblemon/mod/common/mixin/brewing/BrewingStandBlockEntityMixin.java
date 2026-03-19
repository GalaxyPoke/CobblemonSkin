/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.brewing;

import com.cobblemon.mod.common.CobblemonRecipeTypes;
import com.cobblemon.mod.common.item.crafting.brewingstand.BrewingStandInput;
import com.cobblemon.mod.common.item.crafting.brewingstand.BrewingStandRecipe;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

	@WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;doBrew(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/NonNullList;)V"))
	private static void cobblemon$doBrew(Level level, BlockPos pos, NonNullList<ItemStack> slots, Operation<Void> original) {
		var recipe = cobblemon$fetchBrewingRecipe(slots, level);
		if(recipe == null) {
			original.call(level, pos, slots);
			return;
		}
		//copied from vanilla doBrew logic, but slightly tweaked to work for cobblemon, this was needed as replacing the loop was not quite possible inside doBrew
		ItemStack itemStack = slots.get(3);
		for(int i = 0; i < 3; ++i) {
			if (!slots.get(i).isEmpty()){
				slots.set(i, recipe.getResult().copy());
			}
		}

		itemStack.shrink(1);
		if (itemStack.getItem().hasCraftingRemainingItem()) {
			ItemStack itemStack2 = new ItemStack(itemStack.getItem().getCraftingRemainingItem());
			if (itemStack.isEmpty()) {
				itemStack = itemStack2;
			} else {
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), itemStack2);
			}
		}

		slots.set(3, itemStack);
		level.levelEvent(LevelEvent.SOUND_BREWING_STAND_BREW, pos, 0);
	}

	@WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;isBrewable(Lnet/minecraft/world/item/alchemy/PotionBrewing;Lnet/minecraft/core/NonNullList;)Z"))
	private static boolean cobblemon$isBrewable(PotionBrewing potionBrewing, NonNullList<ItemStack> items, Operation<Boolean> original,
												@Local(argsOnly = true) Level level) {
		return cobblemon$fetchBrewingRecipe(items, level) != null || original.call(potionBrewing, items);
	}

	@Unique
	private static BrewingStandRecipe cobblemon$fetchBrewingRecipe(NonNullList<ItemStack> items, Level level) {
		ItemStack ingredient = items.get(3);
		List<ItemStack> bottles = items.subList(0, 3);

		boolean allBottlesEmpty = true;
		for (ItemStack bottle : bottles) {
			if (!bottle.isEmpty()) {
				allBottlesEmpty = false;
				break;
			}
		}

		if (ingredient.isEmpty() || allBottlesEmpty) {
			return null;
		}

		BrewingStandInput input = new BrewingStandInput(ingredient, bottles);
		RecipeManager recipeManager = level.getRecipeManager();

		Optional<RecipeHolder<BrewingStandRecipe>> recipeHolder =
				recipeManager.getRecipeFor(CobblemonRecipeTypes.INSTANCE.getBREWING_STAND(), input, level);

		return recipeHolder.map(RecipeHolder::value).orElse(null);
	}

	@Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
	private void cobblemon$canPlaceItem(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (stack == null || stack.isEmpty()) return;

		BrewingStandBlockEntity self = (BrewingStandBlockEntity) (Object) this;
		Level level = self.getLevel();

		if (level != null) {

			var rm = level.getRecipeManager();

			//Input slot
			if (slot == 3) {
				if (BrewingStandRecipe.Companion.isInput(stack, rm)) {
					cir.setReturnValue(true);
					return;
				}
			}
			// Output slots
			if (slot >= 0 && slot < 3) {
				if (BrewingStandRecipe.Companion.isBottle(stack, rm)) {
					if (self.getItem(slot).isEmpty()) {
						cir.setReturnValue(true);
					} else {
						cir.setReturnValue(false);
					}
				}
			}
		}
	}
}