/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.ai.EntityBehaviour;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;



@Mixin(targets = "net.minecraft.world.entity.monster.Phantom$PhantomSweepAttackGoal")
public abstract class PhantomSweepAttackGoalMixin extends Goal {

    @Unique
    private Phantom phantomSelf; // Allows access to the outer class

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(Phantom phantom, CallbackInfo ci) {
        this.phantomSelf = phantom;
    }

    @Inject(method = "canContinueToUse", at = @At("RETURN"), cancellable = true)
    private void modifyCanContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        Phantom phantom = this.phantomSelf; // Outer class instance
        if(phantom == null) return;

        if (cir.getReturnValue()) {
            var nearbyFearedEntities = phantom.level().getEntities(phantom, phantom.getBoundingBox().inflate(16.0),
                    (entity -> ((entity instanceof PokemonEntity && ((PokemonEntity) entity).getBeamMode() == 0 && ((PokemonEntity) entity).getBehaviour().getEntityInteract().getAvoidedByPhantom())
                            || (entity instanceof ServerPlayer && EntityBehaviour.Companion.hasPhantomFearedShoulderMount((ServerPlayer)entity)))));
            if (!nearbyFearedEntities.isEmpty()) {
               nearbyFearedEntities.forEach(entity -> {
                   if (entity instanceof PokemonEntity) {
                       ((PokemonEntity) entity).cry();
                   }
                   //TODO: Find a way to make shoulder mounted Pokemon cry
               });
                cir.setReturnValue(false);
            }
        }
    }
}

