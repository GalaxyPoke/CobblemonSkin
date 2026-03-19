/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.client.render.player.MountedPlayerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin adds to the head of the renderToBuffer function and allows us to detect if it is a HumanoidModel and
 * is being rendered as part of a player in the world who is riding a PokemonEntity. If so we need to obey any
 * root animations that have been applied earlier.
 *
 * @author Hiroku
 * @since May 16, 2025
 */
@Mixin(AgeableListModel.class)
public class AgeableListModelMixin {
    @Inject(method = "renderToBuffer", at = @At("HEAD"))
    private void cobblemon$renderToBuffer(
        PoseStack poseStack,
        VertexConsumer vertexConsumer,
        int packedLight,
        int packedOverlay,
        int colour,
        CallbackInfo ci
    ) {
        Model model = (Model) (Object) this;
        if (model instanceof HumanoidModel<?> && MountedPlayerRenderer.shouldApplyRootAnimation) {
            MountedPlayerRenderer.INSTANCE.animateRoot(poseStack);
        }
    }
}
