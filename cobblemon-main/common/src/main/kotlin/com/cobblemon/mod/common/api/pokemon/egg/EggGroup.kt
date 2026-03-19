/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.egg

/**
 * These represent categories which determine which Pokémon are able to interbreed.
 *
 * See the [Bulbapedia](https://bulbapedia.bulbagarden.net/wiki/Egg_Group) article for more information.
 *
 * @property showdownID used for data synchronization with Showdown data format.
 */
enum class EggGroup(val showdownID: String) {

    MONSTER("Monster"),
    WATER_1("Water 1"),
    BUG("Bug"),
    FLYING("Flying"),
    FIELD("Field"),
    FAIRY("Fairy"),
    GRASS("Grass"),
    HUMAN_LIKE("Human-Like"),
    WATER_3("Water 3"),
    MINERAL("Mineral"),
    AMORPHOUS("Amorphous"),
    WATER_2("Water 2"),
    DITTO("Ditto"),
    DRAGON("Dragon"),
    UNDISCOVERED("Undiscovered");

    companion object {
        private val identifierMap: Map<String, EggGroup> = entries.associateBy {
            it.showdownID.lowercase().replace(" ", "_").replace("-", "_")
        }

        fun fromIdentifier(identifier: String): EggGroup? {
            val key = identifier.lowercase().replace(" ", "_").replace("-", "_")
            return identifierMap[key]
        }
    }
}