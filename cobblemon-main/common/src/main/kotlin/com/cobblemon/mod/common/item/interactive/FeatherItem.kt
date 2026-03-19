/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import net.minecraft.sounds.SoundEvent

class FeatherItem(stat: Stat) : EVIncreaseItem(stat, 1) {
    override val sound: SoundEvent = CobblemonSounds.MEDICINE_FEATHER_USE
}