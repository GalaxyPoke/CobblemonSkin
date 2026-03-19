/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(BeehiveBlockEntity.class)
public interface BeehiveBlockEntityAccessor {
    @Accessor("stored")
    List<Object> getStored();
    @Accessor("savedFlowerPos")
    BlockPos savedFlowerPos();


    /**
     * Access the private static method:
     * private static void releaseOccupant(Level level, BlockPos pos, BeeData beeData, ...)
     */
    @Invoker("releaseOccupant")
    static boolean releaseOccupant(Level level, BlockPos pos, BlockState state, BeehiveBlockEntity.Occupant occupant, @Nullable List<Entity> storedInHives, BeehiveBlockEntity.BeeReleaseStatus releaseStatus, @Nullable BlockPos storedFlowerPos) {
        throw new AssertionError(); // Mixin will overwrite
    }
}

