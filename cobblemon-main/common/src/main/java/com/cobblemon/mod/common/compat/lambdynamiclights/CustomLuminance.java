/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.compat.lambdynamiclights;

import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.lighthing.LightingData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class CustomLuminance {
    protected static Optional<Integer> extractFormLightLevel(@NotNull FormData form, boolean underwater) {
        if (form.getLightingData() == null || !liquidGlowModeSupport(form.getLightingData().getLiquidGlowMode(), underwater)) {
            return Optional.empty();
        }
        return Optional.of(form.getLightingData().getLightLevel());
    }

    protected static boolean liquidGlowModeSupport(@NotNull LightingData.LiquidGlowMode liquidGlowMode, boolean underwater) {
        return underwater ? liquidGlowMode.getGlowsUnderwater() : liquidGlowMode.getGlowsInLand();
    }
}
