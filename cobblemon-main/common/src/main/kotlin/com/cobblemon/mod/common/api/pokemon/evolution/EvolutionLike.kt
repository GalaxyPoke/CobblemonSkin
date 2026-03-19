/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.evolution

/**
 * Represents the base of an evolution.
 * It can either be the full data container present on the server side [Evolution].
 * Or the client side representation for display [EvolutionDisplay].
 *
 * @author Licious
 * @since April 28th, 2022
 */
interface EvolutionLike {

    /**
     * The unique id of the evolution.
     * It should be human-readable, I.E pikachu_level
     */
    val id: String

}