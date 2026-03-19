/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.junit.BootstrapMinecraft
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@BootstrapMinecraft
internal class PokemonPropertiesKtTest {

    @Test
    fun `validate Serialization and Deserialization`() {
        Cobblemon.loadConfig()

        val a = PokemonProperties.parse("bidoof level=10 attack_iv=31")
        assertEquals(10, a.level)
        assertEquals(31, a.ivs?.get(Stats.ATTACK))
        assertEquals(null, a.ivs?.get(Stats.DEFENCE))

        val b = a.copy()
        assertEquals(10, b.level)
        assertEquals(31, b.ivs?.get(Stats.ATTACK))
        assertEquals(null, b.ivs?.get(Stats.DEFENCE))
    }

}