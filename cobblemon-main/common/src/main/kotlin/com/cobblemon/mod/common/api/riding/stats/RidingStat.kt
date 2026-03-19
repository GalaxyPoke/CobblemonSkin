/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.stats

import com.cobblemon.mod.common.api.cooking.Flavour
import com.cobblemon.mod.common.util.lang
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

/**
 * A stat that is used for riding Pokémon. The exact implementation of each of these depends on the [com.cobblemon.mod.common.api.riding.controller.RideController].
 *
 * @author Hiroku
 * @since February 17th, 2025
 */
enum class RidingStat(
    val flavour: Flavour
) {
    ACCELERATION(Flavour.SPICY),
    SKILL(Flavour.DRY),
    SPEED(Flavour.SWEET),
    STAMINA(Flavour.SOUR),
    JUMP(Flavour.BITTER);

    companion object {
        fun getByName(name: String) = RidingStat.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
        fun getByFlavour(flavour: Flavour) = RidingStat.entries.firstOrNull { it.flavour == flavour }
    }

    val displayName: MutableComponent
        get() = lang("ui.stats.ride.${name.lowercase()}")
}