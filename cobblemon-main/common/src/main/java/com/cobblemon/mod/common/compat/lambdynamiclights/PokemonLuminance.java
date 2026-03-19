/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.compat.lambdynamiclights;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class PokemonLuminance extends CustomLuminance implements EntityLuminance {
    public static final PokemonLuminance INSTANCE = new PokemonLuminance();

    private PokemonLuminance() {}

    @Override
    public @NotNull Type type() {
        return LambDynamicLightsInitializer.POKEMON_LUMINANCE;
    }

    @Override
    public @Range(from = 0L, to = 15L) int getLuminance(
            @NotNull ItemLightSourceManager itemLightSourceManager,
            @NotNull Entity entity
    ) {
        if (entity instanceof PokemonEntity pokemon) {
            boolean underwater = pokemon.isUnderWater();
            ItemStack item = pokemon.getShownItem();
            int itemLightLevel = item.isEmpty() ? 0 : itemLightSourceManager.getLuminance(item, underwater);
            int formLightLevel = extractFormLightLevel(pokemon.getForm(), underwater).orElse(0);

            return Math.max(itemLightLevel, formLightLevel);
        } else {
            return 0;
        }
    }
}
