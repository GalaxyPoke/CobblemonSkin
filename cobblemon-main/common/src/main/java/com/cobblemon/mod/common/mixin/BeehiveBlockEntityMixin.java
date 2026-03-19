/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;


import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.mixin.accessor.BeeDataAccessor;
import com.cobblemon.mod.common.mixin.accessor.BeehiveBlockEntityAccessor;
import com.cobblemon.mod.common.mixin.accessor.BlockEntityAccessor;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(BeehiveBlockEntity.class)
public abstract class BeehiveBlockEntityMixin {


    // Accessor for the private 'stored' field in BeehiveBlockEntity
    @Accessor("stored")
    public abstract List<BeeDataAccessor> getStored();

    @Inject(method = "emptyAllLivingFromHive", at = @At("HEAD"))
    private void cobblemon$emptyAllLivingFromHive(@Nullable Player player, BlockState state, BeehiveBlockEntity.BeeReleaseStatus releaseStatus, CallbackInfo ci) {
        if (player != null) {
            final BeehiveBlockEntity blockEntity = (BeehiveBlockEntity) (Object) this;
            // if the block is sedated or the player is too far away, no aggro should be applied
            if (blockEntity.isSedated() || player.position().distanceToSqr(blockEntity.getBlockPos().getCenter()) > 16.0)
                return;
            // from this point need to access private members of the block entity
            final BeehiveBlockEntityAccessor blockEntityAccessor = (BeehiveBlockEntityAccessor) this;
            List<Object> storedList = blockEntityAccessor.getStored();

            List<Entity> list = Lists.newArrayList();
            storedList.removeIf((beeData) -> {
                        BeehiveBlockEntity.Occupant occupant = ((BeeDataAccessor) beeData).invokeToOccupant();
                        // Checking the for the tag allows us to make sure it's one of our pokemon before trying to rehydrate it into an entity
                        boolean isPokemon = false;
                        if (occupant.entityData().contains("id")) {
                            isPokemon = "cobblemon:pokemon".equals(occupant.entityData().getUnsafe().getString("id"));
                        }
                        if (isPokemon) {
                            return BeehiveBlockEntityAccessor.releaseOccupant(
                                    ((BlockEntityAccessor) blockEntityAccessor).getLevel(),
                                    ((BlockEntityAccessor) blockEntityAccessor).getWorldPosition(),
                                    state,
                                    occupant,
                                    list,
                                    releaseStatus,
                                    blockEntityAccessor.savedFlowerPos());
                        } else {
                            return false;
                        }
                    }
            );
            for (Entity entity : list) {
                if (entity instanceof PokemonEntity) {
                    ((PokemonEntity) entity).getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, player);
                }
            }
        }
    }

    @Redirect(
            method = "releaseOccupant",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private static boolean cobblemon$captureReleasedEntity(
            Level level, Entity entity,
            Level lvl, BlockPos pos, BlockState state,
            BeehiveBlockEntity.Occupant occupant, @Nullable List<Entity> bees,
            BeehiveBlockEntity.BeeReleaseStatus releaseStatus, @Nullable BlockPos flowerPos) {

        if (bees != null && !bees.contains(entity)) {
            // Avoid duplicates if vanilla also adds bees to the list on your version
            bees.add(entity);
        }
        return level.addFreshEntity(entity);
    }
}

