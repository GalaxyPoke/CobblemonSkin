/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching.effects

import com.cobblemon.mod.common.api.pokeball.catching.CaptureEffect

/**
 * A collection of some high order functions of [CaptureEffect]s.
 *
 * @author Licious
 * @since May 8th, 2022
 */
object CaptureEffects {

    /**
     * Used by [PokeBalls.HEAL_BALL].
     * Heals, removes status & fully restores the PP of the captured Pokémon.
     */
    val FULL_RESTORE = CaptureEffect { _, pokemon -> pokemon.heal() }

    /**
     * Used by [PokeBalls.FRIEND_BALL].
     *
     * @param value The value the friendship of the Pokémon will start at must be between 0 and 255.
     * @return The created capture effect.
     */
    fun friendshipSetter(value: Int) = CaptureEffect { _, pokemon -> pokemon.setFriendship(value) }

}
