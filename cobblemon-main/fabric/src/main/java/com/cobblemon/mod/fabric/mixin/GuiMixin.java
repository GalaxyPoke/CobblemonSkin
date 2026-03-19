/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.fabric.mixin;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.BooleanSupplier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to give us a hook to render the PartyOverlay below the Chat
 *
 * @author Qu
 * @since 2022-02-22
 */
@Mixin(Gui.class)
public class GuiMixin {
    private Long lastTimeMillis = null;

    @Inject(method = "renderCameraOverlays", at = @At("HEAD"))
    private void beforeChatHook(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (lastTimeMillis != null) {
            CobblemonClient.INSTANCE.beforeChatRender(context, (System.currentTimeMillis() - lastTimeMillis) / 1000F * 20);
        }
        lastTimeMillis = System.currentTimeMillis();
    }

    //Modifies the render condition passed in the LayeredDraw so the layer doesnt render when pokedex is open
    @WrapOperation(
        method = "<init>(Lnet/minecraft/client/Minecraft;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDraw;add(Lnet/minecraft/client/gui/LayeredDraw;Ljava/util/function/BooleanSupplier;)Lnet/minecraft/client/gui/LayeredDraw;")
    )
    private LayeredDraw cobblemon$dontRenderUiInPokedex(
        LayeredDraw instance,
        LayeredDraw layeredDraw,
        BooleanSupplier renderInner,
        Operation<LayeredDraw> original
    ) {
        BooleanSupplier newBool = () -> {
            if (CobblemonClient.INSTANCE.getPokedexUsageContext().getScanningGuiOpen() && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                return false;
            }
            return renderInner.getAsBoolean();
        };
        return original.call(instance, layeredDraw, newBool);
    }
}
