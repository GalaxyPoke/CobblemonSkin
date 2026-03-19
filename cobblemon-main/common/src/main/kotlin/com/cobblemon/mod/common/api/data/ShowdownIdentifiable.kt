/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.data

/**
 * Represents an object that has a showdown ID attached to itself.
 *
 * @author Licious
 * @since January 28th, 2022
 */
interface ShowdownIdentifiable {

    /**
     * Converts the implementation to literal Showdown ID.
     *
     * @return The literal Showdown ID.
     */
    fun showdownId(): String

    companion object {

        internal val REGEX = Regex("[^a-z0-9]+")

    }

}