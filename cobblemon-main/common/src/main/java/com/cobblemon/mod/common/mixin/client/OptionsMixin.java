/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.OrientationControllable;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public abstract class OptionsMixin {

    /**
     * Only allow third person on rollables when motion sick cam is enabled
     */
    @Inject(
            method = "getCameraType",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cobblemon$forceThirdPerson(CallbackInfoReturnable<CameraType> cir) {
        var mc = Minecraft.getInstance();
        if(!(mc.player instanceof Player player)) return;
        if(!(player.getVehicle() instanceof OrientationControllable vehicle)) return;
        if(vehicle.getOrientationController() == null) return;

        // Force third person if on an active rollable vehicle and roll is disabled.
        if (Cobblemon.config.getDisableRoll() && vehicle.getOrientationController().isActive()) {
            cir.setReturnValue(CameraType.THIRD_PERSON_BACK);
            cir.cancel();
        }
    }
}
