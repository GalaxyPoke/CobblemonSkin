/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.api.scheduling.ClientTaskTracker;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Unique
    long lastTime = -1;

    @Inject(
            method = "render",
            at = @At(value = "TAIL")
    )
    public void render(DeltaTracker counter, boolean tick, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        long newTime = System.currentTimeMillis();
        // Don't play scheduled animations when the game is paused
        if (client.isPaused()) {
            lastTime = newTime;
            return;
        }

        if (lastTime != -1) {
            ClientTaskTracker.INSTANCE.update((newTime - lastTime) / 1000F);
        }
        lastTime = newTime;
    }

    @ModifyArg(
        method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"
        ),
        index = 4
    )
    private Predicate<Entity> filterPokemonVehicle(Predicate<Entity> original, @Local(ordinal = 0, argsOnly = true) Entity entity) {
        return (entityX) -> !(entity.getVehicle() == entityX && entityX instanceof PokemonEntity) && original.test(entityX);
    }

}
