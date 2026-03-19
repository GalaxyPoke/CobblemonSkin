/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.lighthing

/**
 * Represents light emitting properties of a species/form.
 * This has no use in the base mod and is instead used for dynamic lighting mod compatibility.
 *
 * @property lightLevel The light level emitted.
 * @property liquidGlowMode The [LiquidGlowMode] for this effect.
 */
data class LightingData(val lightLevel: Int, val liquidGlowMode: LiquidGlowMode) {

    /**
     * Represents if a [LightingData] is applied while in land or underwater
     *
     * @property glowsInLand If this allows activation while on land.
     * @property glowsUnderwater If this allows activation while underwater.
     */
    @Suppress("unused")
    enum class LiquidGlowMode(val glowsInLand: Boolean, val glowsUnderwater: Boolean) {

        LAND(true, false),
        UNDERWATER(false, true),
        BOTH(true, true)

    }

}
