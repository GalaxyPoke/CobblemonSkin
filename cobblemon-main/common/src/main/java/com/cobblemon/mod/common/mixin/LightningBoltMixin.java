/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;


@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin {

    @Inject(method = "spawnFire", at = @At(value = "Head"), cancellable = true)
    private void cobblemon$spawnFire(int extraIgnitions, CallbackInfo ci) {
        // This prevents fire from spawning around Pokemon who otherwise should be immune to lightning and effectively absorb the hit
        final LightningBolt lightningBolt = (LightningBolt) (Object) this;
        List<Entity> list = lightningBolt.level().getEntities(
                lightningBolt,
                new AABB(lightningBolt.getX() - 2.0, lightningBolt.getY() - 2.0, lightningBolt.getZ() - 2.0, lightningBolt.getX() + 2.0, lightningBolt.getY() + 2.0, lightningBolt.getZ() + 2.0),
                it -> {
                    if (!it.isAlive() || !(it instanceof PokemonEntity)) {
                        return false;
                    }
                    String abilityName = ((PokemonEntity) it).getPokemon().getAbility().getName();
                    return abilityName.equals("lightningrod") || abilityName.equals("voltabsorb") || abilityName.equals("motordrive");
                });
        if (!list.isEmpty()) {
            ci.cancel();
        }
    }
}