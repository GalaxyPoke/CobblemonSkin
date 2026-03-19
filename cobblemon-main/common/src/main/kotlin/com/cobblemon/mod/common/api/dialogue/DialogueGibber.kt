/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.dialogue

import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation

/**
 * Settings for the gibbering sound effect that plays gradually as dialogue appears.
 *
 * @author Hiroku, landonjw
 * @since April 19th, 2025
 */
class DialogueGibber(
    /** Whether text will gradually appear while the gibbering occurs. */
    val graduallyShowText: Boolean = true,
    /** Whether clicking will make the gibber skip to the end. */
    val allowSkip: Boolean = true,
    /** The space between gibbers in terms of characters. 'Holy cow' would play 2 sounds if step = 4. */
    val step: Int = 4,
    /** The time (in seconds) between gibbers. The smaller this is, the faster the text will progress. */
    val interval: Double = 0.1,
    val minPitch: Float = 0.9F,
    val maxPitch: Float = 1.1F,
    val minVolume: Float = 0.9F,
    val maxVolume: Float = 1.0F,
    /** The possible sounds to play when gibbering. */
    val sounds: List<ResourceLocation> = listOf(
        cobblemonResource("entity.npc.gibber.generic")
    )
)