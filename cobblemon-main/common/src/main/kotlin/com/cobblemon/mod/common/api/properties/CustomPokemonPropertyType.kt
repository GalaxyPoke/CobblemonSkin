/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.properties

/**
 * A provider of a particular sort of [CustomPokemonProperty]. This interface
 * provides the means to parse a new property of the implementing generic type.
 *
 * @author Hiroku
 * @since February 12th, 2022
 */
interface CustomPokemonPropertyType<T : CustomPokemonProperty> {
    /** All the keys that will match this property type. */
    val keys: Iterable<String>
    /**
     * Whether or not an argument needs to include a key to have this type try parsing it. If this is false,
     * [fromString] will be run even if none of the [keys] was present in the argument.
     */
    val needsKey: Boolean
    /** Tries parsing a new instance of this generic type based off a nullable string. */
    fun fromString(value: String?):  T?

    /**
     * Returns a list of literal examples of the values this property will accept.
     * This may not contain every possible value, the intent is for tab completion when using a PokemonProperty argument in a command.
     *
     * @return A list of literal examples.
     */
    fun examples(): Collection<String>

}