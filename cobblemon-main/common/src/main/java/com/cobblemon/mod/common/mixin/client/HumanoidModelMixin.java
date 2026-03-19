/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.OrientationControllable;
import com.cobblemon.mod.common.client.render.player.MountedPlayerRenderer;
import com.cobblemon.mod.common.duck.RidePassenger;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
    @Shadow @Final
    public ModelPart head;
    @Shadow @Final
    public ModelPart hat;
    @Shadow @Final
    public ModelPart rightArm;
    @Shadow @Final
    public ModelPart leftArm;
    @Shadow @Final
    public ModelPart rightLeg;
    @Shadow @Final
    public ModelPart leftLeg;
    @Shadow @Final
    public ModelPart body;

    @Inject(method = "setupAnim*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;copyFrom(Lnet/minecraft/client/model/geom/ModelPart;)V", shift = At.Shift.BEFORE))
    public void cobblemon$animateRiders(
        LivingEntity entity,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        CallbackInfo ci
    ) {
        MountedPlayerRenderer.shouldApplyRootAnimation = false;

        if (MountedPlayerRenderer.relevantPartsByName.size() <= 1) {
            MountedPlayerRenderer.relevantPartsByName.put("arm_left", leftArm);
            MountedPlayerRenderer.relevantPartsByName.put("arm_right", rightArm);
            MountedPlayerRenderer.relevantPartsByName.put("leg_left", leftLeg);
            MountedPlayerRenderer.relevantPartsByName.put("leg_right", rightLeg);
            MountedPlayerRenderer.relevantPartsByName.put("head", head);
            MountedPlayerRenderer.relevantPartsByName.put("hat", hat);
            MountedPlayerRenderer.relevantPartsByName.put("torso", body);
        }

        Entity vehicle = entity.getVehicle();
        if (vehicle instanceof PokemonEntity pokemonEntity && entity instanceof OrientationControllable) {
            var shouldRotatePlayerHead = pokemonEntity.ifRidingAvailableSupply(false, (behaviour, settings, state) ->
                    behaviour.shouldRotateRiderHead(settings, state, pokemonEntity)
            );

            // If the player is on a rollable ride then use the ride local rotations to rotate the head
            // This is to allow the player head to face the correct direction when "freelooking"
            // TODO: find a way for this to work on built jars without borking head rotations.
//            if (entity instanceof RidePassenger playerRotater &&
//                vehicle instanceof OrientationControllable controller &&
//                controller.getOrientationController().isActive()) {
//                this.head.xRot = playerRotater.cobblemon$getRideXRot();
//                headPitch = playerRotater.cobblemon$getRideXRot();
//                this.head.yRot = playerRotater.cobblemon$getRideYRot();
//                netHeadYaw = playerRotater.cobblemon$getRideYRot();
//            }

            if (!shouldRotatePlayerHead) {
                netHeadYaw = 0f;
                headPitch = 0f;
                if (this.head != null) {
                    this.head.yRot = 0f;
                    this.head.xRot = 0f;
                }
            }

            if (!(entity instanceof AbstractClientPlayer player)) return;

            MountedPlayerRenderer.shouldApplyRootAnimation = true;
            MountedPlayerRenderer.INSTANCE.animate((HumanoidModel<AbstractClientPlayer>)(Object)this, pokemonEntity, player, netHeadYaw, headPitch, ageInTicks, limbSwing, limbSwingAmount);
        }
    }
}