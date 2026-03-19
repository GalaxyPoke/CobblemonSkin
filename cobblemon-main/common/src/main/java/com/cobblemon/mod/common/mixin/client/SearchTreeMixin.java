/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.client.searchtree.SearchTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mixin(IdSearchTree.class)
public abstract class SearchTreeMixin {
    @Unique
    private static final ThreadLocal<Boolean> reentryFlag = ThreadLocal.withInitial(() -> false);

    /**
     * Runs an additional search using the original search term where every instance of 'e' is replaced with 'é'
     * Appends additional search results to original search results.
     */
    @Inject(method = "search(Ljava/lang/String;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void onSearch(String string, CallbackInfoReturnable<List> cir) {
        // If flag is true, skip to avoid infinite loop
        if (reentryFlag.get()) {
            return;
        }

        try {
            reentryFlag.set(true);
            List originalResults = cir.getReturnValue();

            if (!string.contains("poke")) {
                return;
            }

            String modifiedString = string.replace("poke", "poké");
            List additionalResults = ((SearchTree)this).search(modifiedString);

            Set combinedSet = new LinkedHashSet<>();
            combinedSet.addAll(originalResults);
            combinedSet.addAll(additionalResults);

            cir.setReturnValue(new ArrayList<>(combinedSet));
        } finally {
            reentryFlag.set(false);
        }
    }
}
