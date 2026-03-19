/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge.mixin.client;

import com.cobblemon.mod.common.client.render.camera.MountedCameraRenderer;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class NeoforgeCameraMixin {
    //This is linted as an "unnecessary qualifier", but it is in fact necessary, we need to target the 3 parameter version in NeoForge
    //I think its some weirdness with Fabric only having one setRotation method, and IntelliJ is looking at the fabric sources
    @Inject(method = "Lnet/minecraft/client/Camera;setRotation(FFF)V", at = @At("HEAD"), cancellable = true)
    public void cobblemon$setRotation(float f, float g, float h, CallbackInfo ci) {
        if (MountedCameraRenderer.INSTANCE.setRotation((Camera) (Object) this)) ci.cancel();
    }
}
