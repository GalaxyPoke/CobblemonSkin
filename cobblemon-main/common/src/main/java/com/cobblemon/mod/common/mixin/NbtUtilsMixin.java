/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import static com.cobblemon.mod.common.datafixer.CobblemonSchemas.VERSION_KEY;

import com.cobblemon.mod.common.datafixer.CobblemonSchemas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtUtils.class)
public class NbtUtilsMixin {
    @Inject(
        method = "addCurrentDataVersion(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;",
        at = @At("TAIL")
    )
    private static void addCobblemonDataVersion(
        CompoundTag tag,
        CallbackInfoReturnable<CompoundTag> cir
    ) {
        tag.putInt(VERSION_KEY, CobblemonSchemas.DATA_VERSION);
    }
}
