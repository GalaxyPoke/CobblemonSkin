/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage

//internal class PokemonStoreTest {
//
//    @Test
//    fun `removing pokemon that is in another storage with different type returns false`() {
//        val pokemonStore = mockk<PokemonStore<PartyPosition>>()
//        every { pokemonStore.remove(any<Pokemon>()) }.answers{ callOriginal() }
//        val pcStore = mockk<PokemonStore<PCPosition>>()
//        val storeCoordinates = StoreCoordinates(
//            store = pcStore,
//            position = PCPosition(1, 1)
//        )
//        val pokemon = Pokemon().apply {
//            this.storeCoordinates.set(storeCoordinates)
//        }
//        val result = pokemonStore.remove(pokemon)
//        assertFalse(result)
//    }
//
//}