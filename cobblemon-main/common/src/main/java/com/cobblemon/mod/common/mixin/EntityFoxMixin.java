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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Fox.class)
public class EntityFoxMixin extends Mob {

    private EntityFoxMixin(net.minecraft.world.entity.EntityType<? extends Creeper> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "registerGoals", at = @At(value = "TAIL"), cancellable = false)
    private void cobblemon$initGoals(CallbackInfo callbackInfo) {
        final Fox fox = (Fox) (Object) this;
        // Pokemon Entities
        this.goalSelector.addGoal(
            4,
            new AvoidEntityGoal<>(
                    fox,
                PokemonEntity.class,
                8.0f,
                1.6,
                1.4,
                 entity -> ((PokemonEntity)entity).getBehaviour().getEntityInteract().getAvoidedByFox() && ((PokemonEntity)entity).getBeamMode() != 1
            )
        );

        // Players with shoulder mounted Pokemon
        this.goalSelector.addGoal(
            3,
            new AvoidEntityGoal<>(
                fox,
                ServerPlayer.class,
                8.0f,
                1.6,
                1.4,
                entity -> EntityBehaviour.Companion.hasFoxFearedShoulderMount((ServerPlayer)entity)
            )
        );

    }
}

