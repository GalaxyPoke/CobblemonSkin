/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge.mixin.client;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.neoforged.neoforge.client.gui.GuiLayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class GuiMixin {
    //Modifies the render condition passed in the LayeredDraw so the layer doesnt render when pokedex is open
    @WrapOperation(
        method = "<init>(Lnet/minecraft/client/Minecraft;)V",
        at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/gui/GuiLayerManager;add(Lnet/neoforged/neoforge/client/gui/GuiLayerManager;Ljava/util/function/BooleanSupplier;)Lnet/neoforged/neoforge/client/gui/GuiLayerManager;")
    )
    private GuiLayerManager cobblemon$dontRenderUiInPokedex(
        GuiLayerManager instance,
        GuiLayerManager guiLayerManager,
        BooleanSupplier child,
        Operation<GuiLayerManager> original
    ) {
        BooleanSupplier newChild = () -> {
            if (CobblemonClient.INSTANCE.getPokedexUsageContext().getScanningGuiOpen() && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                return false;
            }
            return child.getAsBoolean();
        };
        return original.call(instance, guiLayerManager, newChild);
    }
}
