/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;


@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(method = "findLightningRod", at = @At(value = "RETURN"), cancellable = true)
    private void cobblemon$findLightningRod(BlockPos blockPos, CallbackInfoReturnable<Optional<BlockPos>> ci) {
        final ServerLevel serverLevel = (ServerLevel) (Object) this;
        if (ci.getReturnValue().isEmpty()) {
            // Search for Pokemon with the ability Lightning Rod and redirect to them
            AABB aABB = AABB.encapsulatingFullBlocks(blockPos, new BlockPos(blockPos.atY(serverLevel.getMaxBuildHeight()))).inflate(64.0);
            List<LivingEntity> list = serverLevel.getEntitiesOfClass(LivingEntity.class, aABB, (livingEntity) -> livingEntity != null
                    && livingEntity.isAlive()
                    && livingEntity instanceof PokemonEntity
                    && ((PokemonEntity) livingEntity).getBeamMode() == 0
                    && ((PokemonEntity) livingEntity).getPokemon().getAbility().getName().equals("lightningrod"));
            if (!list.isEmpty()) {
                ci.setReturnValue(Optional.of((list.get(serverLevel.random.nextInt(list.size()))).blockPosition()));
            }
        }
    }
}
