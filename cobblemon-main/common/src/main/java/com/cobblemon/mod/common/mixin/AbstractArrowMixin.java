/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.OrientationControllable;
import com.cobblemon.mod.common.duck.RidePassenger;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL")
    )
    private void cobblemon$adjustSpawnPosition(
            EntityType<? extends AbstractArrow> entityType,
            LivingEntity owner,
            Level level,
            ItemStack pickupItemStack,
            @Nullable ItemStack firedFromWeapon,
            CallbackInfo ci
    ) {
        AbstractArrow arrow = (AbstractArrow)(Object)this;
        if(!(owner.getVehicle() instanceof OrientationControllable vehicle)) return;
        if(!(vehicle instanceof PokemonEntity)) return;
        var vehicleController = vehicle.getOrientationController();

        // If this is a pokemon ride passenger then use the rider eye position to set the arrow spawn position.
        if (owner instanceof RidePassenger ridePassenger && vehicleController != null ) {
            var eyePos = ridePassenger.cobblemon$getRideEyePos(); //.add(0.0, -2.0, 0.0);
            arrow.setPos(eyePos);
        }
    }
}
