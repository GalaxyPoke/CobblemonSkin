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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
public abstract class PowderSnowBlockMixin {
    @Inject(method = "canEntityWalkOnPowderSnow", at = @At(value = "RETURN"), cancellable = true)
    private static void cobblemon$canEntityWalkOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> ci) {
        if (ci.getReturnValue() == false && entity instanceof MoLangScriptingEntity) {
            ci.setReturnValue(((MoLangScriptingEntity) entity).getConfig().getMap().getOrDefault("can_stand_on_powder_snow", DoubleValue.ZERO).asDouble() == 1.0);
        }
    }
}
