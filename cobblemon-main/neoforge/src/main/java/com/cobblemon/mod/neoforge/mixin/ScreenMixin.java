/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge.mixin;

import com.cobblemon.mod.common.client.gui.CobblemonRenderable;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Necessary due to Neoforge fixing a vanilla bug. Causes disparity between NF and Fabric.
 * Can be removed in 1.21.2+, where vanilla bug is fixed
 */
@Mixin(Screen.class)
public class ScreenMixin {
    @WrapMethod(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
        if (this instanceof CobblemonRenderable) {
            original.call(guiGraphics, mouseX, mouseY, Minecraft.getInstance().getTimer().getRealtimeDeltaTicks());
            return;
        }
        original.call(guiGraphics, mouseX, mouseY, partialTick);
    }

    @WrapMethod(method = "renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")
    public void renderWithTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
        if (this instanceof CobblemonRenderable) {
            original.call(guiGraphics, mouseX, mouseY, Minecraft.getInstance().getTimer().getRealtimeDeltaTicks());
            return;
        }
        original.call(guiGraphics, mouseX, mouseY, partialTick);
    }
}
