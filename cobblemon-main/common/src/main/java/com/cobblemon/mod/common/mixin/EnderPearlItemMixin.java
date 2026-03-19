/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;

@Mixin(EnderpearlItem.class)
public abstract class EnderPearlItemMixin {
    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    private void cobblemon$use(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        double range = player.entityInteractionRange();
        Entity closestEntity = level.getEntities(player, AABB.ofSize(player.position(), range, range, range)).stream()
            .filter(entity -> isLookingAt(player, entity))
            .min(Comparator.comparingDouble(entity -> entity.distanceTo(player)))
            .orElse(null);

        if (closestEntity instanceof PokemonEntity pokemonEntity) {
            if (player.isCrouching() && pokemonEntity.getOwnerUUID() == player.getUUID()) {
                cir.cancel();
            }
        }
    }

    public boolean isLookingAt(Player player, Entity other) {
        float maxDistance = 10F;
        float stepDistance = 0.1F;
        double step = stepDistance;
        Vec3 startPos = player.getEyePosition();
        Vec3 direction = player.getLookAngle();

        while (step <= maxDistance) {
            Vec3 location = startPos.add(direction.scale(step));
            step += stepDistance;

            if (other.getBoundingBox().contains(location)) {
                return true;
            }
        }
        return false;
    }
}
