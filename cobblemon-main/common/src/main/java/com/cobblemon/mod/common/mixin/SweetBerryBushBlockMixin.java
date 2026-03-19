/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.bedrockk.molang.runtime.value.DoubleValue;
import com.cobblemon.mod.common.entity.MoLangScriptingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(SweetBerryBushBlock.class)
public abstract class SweetBerryBushBlockMixin {

    @Inject(method = "entityInside", at = @At(value = "HEAD"), cancellable = true)
    private void cobblemon$entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo callbackInfo) {
        if (entity instanceof MoLangScriptingEntity) {
            if (((MoLangScriptingEntity) entity).getConfig().getMap().getOrDefault("immune_to_sweet_berry_bush_block", DoubleValue.ZERO).asDouble() == 1.0) {
                callbackInfo.cancel();
            }
        }
    }
}
