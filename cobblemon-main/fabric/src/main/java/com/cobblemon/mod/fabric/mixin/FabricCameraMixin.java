/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.mixin;

import com.cobblemon.mod.common.client.render.camera.MountedCameraRenderer;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class FabricCameraMixin {
    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    public void cobblemon$setRotation(float f, float g, CallbackInfo ci) {
        if (MountedCameraRenderer.INSTANCE.setRotation((Camera) (Object) this)) ci.cancel();
    }
}