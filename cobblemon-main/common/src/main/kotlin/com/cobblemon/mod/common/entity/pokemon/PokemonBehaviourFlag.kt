/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon

/**
 * A short list of true/false properties that can be set on a Pokémon entity. These are
 * for use in some poses and AI cases.
 *
 * This list must not get more than 7 elements! Not without upgrading the flag these are
 * stored in from Byte to something larger.
 *
 * @author Hiroku
 * @since December 16th, 2021
 */
enum class PokemonBehaviourFlag {
    LOOKING,
    EXCITED,
    FLYING,
    PASTURE_CONFLICT;

    val bit: Int = ordinal + 1
}