/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.cooking

import net.minecraft.util.StringRepresentable

/**
 * Represents the different flavors associated with berries and other Pokémon consumables.
 * See the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Flavor) article for more information.
 *
 * @author Licious
 * @since November 28th, 2022
 */
enum class Flavour(val colour: Int): StringRepresentable {
    SPICY(0xFA795E),
    DRY(0x65AFF3),
    SWEET(0xFF82CF),
    BITTER(0x77C96A),
    SOUR(0xF3D465),
    MILD(0x9D99BF);

    override fun getSerializedName() = name
    companion object {
        val CODEC = StringRepresentable.fromEnum<Flavour> { Flavour.entries.toTypedArray() }
    }
}
