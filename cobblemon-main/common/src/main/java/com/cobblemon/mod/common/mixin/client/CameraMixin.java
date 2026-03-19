/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.api.riding.Rideable;
import com.cobblemon.mod.common.client.MountedPokemonAnimationRenderController;
import com.cobblemon.mod.common.client.render.camera.MountedCameraRenderer;
import com.cobblemon.mod.common.duck.CameraDuck;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public abstract class CameraMixin implements CameraDuck {
    @Shadow private float eyeHeight;
    @Shadow private float eyeHeightOld;
    @Shadow private BlockGetter level;
    @Shadow private Entity entity;
    @Final @Shadow private Vector3f forwards;
    @Final @Shadow private Vector3f up;
    @Final @Shadow private Vector3f left;
    @Shadow private float yRot;
    @Shadow private float xRot;

    @Shadow protected abstract void setPosition(Vec3 pos);

    @Override public BlockGetter cobblemon$getLevel() {
       return level;
    }

    @Override public Entity cobblemon$getEntity() {
        return entity;
    }

    @Override public Vector3f cobblemon$getForwards() { return forwards; }

    @Override public Vector3f cobblemon$getUp() { return up; }

    @Override public Vector3f cobblemon$getLeft() { return left; }

    @Override public float cobblemon$getXRot() { return xRot; }
    @Override public void cobblemon$setXRot(float xRot) { this.xRot = xRot; }

    @Override public float cobblemon$getYRot() { return yRot; }
    @Override public void cobblemon$setYRot(float yRot) { this.yRot = yRot; }

    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"))
    public void cobblemon$positionCamera(Camera instance, double x, double y, double z, Operation<Void> original, @Local(ordinal = 1, argsOnly = true) boolean thirdPersonReverse) {
        Entity entity = instance.getEntity();
        Entity vehicle = entity.getVehicle();
        MountedPokemonAnimationRenderController.INSTANCE.reset();

        if(vehicle instanceof Rideable){
            Vec3 position = MountedCameraRenderer.INSTANCE.getCameraPosition(
                instance,
                entity,
                vehicle,
                thirdPersonReverse,
                eyeHeight,
                eyeHeightOld
            );

            if(position != null) {
                setPosition(position);
                return;
            }
        }

        // reset the rotation values if it is now not being ridden.
        MountedCameraRenderer.INSTANCE.setSmoothRotation(null);
        original.call(instance, x, y, z);
    }
}
