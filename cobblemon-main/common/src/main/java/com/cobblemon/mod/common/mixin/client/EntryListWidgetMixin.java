/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.client.gui.ScrollingWidget;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractSelectionList.class)
public abstract class EntryListWidgetMixin {

    @SuppressWarnings("ConstantValue")
    @ModifyExpressionValue(method = "getEntryAtPosition", at = @At(value = "CONSTANT", args = {"intValue=4"}))
    public int cobblemon$adjustOnlyIfNecessary(int original) {
        if(ScrollingWidget.class.isInstance(this)) {
            return 0;
        }

        return original;
    }
}
