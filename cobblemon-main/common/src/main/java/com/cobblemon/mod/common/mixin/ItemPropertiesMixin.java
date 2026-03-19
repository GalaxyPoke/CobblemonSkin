/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemProperties.class)
public class ItemPropertiesMixin {

    // Mixins into the fishing rod's register() lambda expression
    // Without this, the fishing rod item model appears cast when using a PokeRod in the other hand
    @Inject(method = "method_27883", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private static void cobblemon$preventRodMixup(ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity, int i, CallbackInfoReturnable<Float> cir) {
        if (!(livingEntity instanceof Player) || ((Player) livingEntity).fishing instanceof PokeRodFishingBobberEntity) {
            cir.setReturnValue(0.0f);
        }
    }
}
