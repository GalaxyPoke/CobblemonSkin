/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.compat.lambdynamiclights;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.client.render.layer.PokemonOnShoulderRenderer;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class PlayerLuminance extends CustomLuminance implements EntityLuminance {
    public static final PlayerLuminance INSTANCE = new PlayerLuminance();
    private static final TagKey<Item> POKEDEX = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Cobblemon.MODID, "pokedex"));

    @Override
    public @NotNull Type type() {
        return LambDynamicLightsInitializer.PLAYER_LUMINANCE;
    }

    @Override
    public @Range(from = 0L, to = 15L) int getLuminance(@NotNull ItemLightSourceManager itemLightSourceManager, @NotNull Entity entity) {
        if (entity instanceof Player player) {
            final int shoulderLightLevel = shoulderLuminance(itemLightSourceManager, player);

            final int itemLightLevel = player.isHolding(stack -> stack.is(POKEDEX)) ? 13 : 0;

            return Math.max(shoulderLightLevel, itemLightLevel);
        } else {
            return 0;
        }
    }

    private @Range(from = 0L, to = 15L) static int shoulderLuminance(@NotNull ItemLightSourceManager itemLightSourceManager, @NotNull Player player) {
        final var leftShoulder = PokemonOnShoulderRenderer.shoulderDataFrom(player.getShoulderEntityLeft());
        final var rightShoulder = PokemonOnShoulderRenderer.shoulderDataFrom(player.getShoulderEntityRight());

        if (leftShoulder == null && rightShoulder == null) {
            return 0;
        }

        final int leftShoulderLightLevel = extractShoulderLightLevel(leftShoulder, itemLightSourceManager, player.isUnderWater());
        final int rightShoulderLightLevel = extractShoulderLightLevel(rightShoulder, itemLightSourceManager, player.isUnderWater());
        return Math.max(leftShoulderLightLevel, rightShoulderLightLevel);
    }

    private static int extractShoulderLightLevel(@Nullable PokemonOnShoulderRenderer.ShoulderData shoulderData, @NotNull ItemLightSourceManager itemLightSourceManager, boolean underwater) {
        if (shoulderData == null) {
            return 0;
        }

        ItemStack item = shoulderData.getShownItem();
        int itemLightLevel = item.isEmpty() ? 0 : itemLightSourceManager.getLuminance(item, underwater);
        int formLightLevel = extractFormLightLevel(shoulderData.getForm(), underwater).orElse(0);

        return Math.max(itemLightLevel, formLightLevel);
    }
}
