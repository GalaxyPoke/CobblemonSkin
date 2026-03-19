/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.block.HeartyGrainsBlock;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleBlockFeature.class)
public class SimpleBlockFeatureMixin {
    //this mixin inserts the logic needed to ensure that when hearty grains are of a certain age, they place with the upper block too
    //similarly to what the doubleblock logic is here
    @Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;canSurvive(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z", shift = At.Shift.AFTER), cancellable = true)
    public void place(FeaturePlaceContext<SimpleBlockConfiguration> context, CallbackInfoReturnable<Boolean> cir,
                      @Local BlockState blockState, @Local BlockPos blockPos, @Local WorldGenLevel worldGenLevel) {
        if (!(blockState.getBlock() instanceof HeartyGrainsBlock)) {
            return;
        }
        if (blockState.getValue(HeartyGrainsBlock.Companion.getAGE()) <= HeartyGrainsBlock.AGE_AFTER_HARVEST) {
            return; //block is too young to be double
        }
        if (!worldGenLevel.isEmptyBlock(blockPos.above())) {
            cir.setReturnValue(false); //no space for the 2nd block above
            return;
        }
        DoublePlantBlock.placeAt(worldGenLevel, blockState, blockPos, Block.UPDATE_CLIENTS);
        cir.setReturnValue(true);
    }
}
