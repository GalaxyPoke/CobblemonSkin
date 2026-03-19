/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.datafixer.fix

import com.cobblemon.mod.common.util.DataKeys
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Dynamic
import kotlin.jvm.optionals.getOrNull

class HypertrainIVFix(outputSchema: Schema) : PokemonFix(outputSchema) {

    override fun fixPokemonData(dynamic: Dynamic<*>): Dynamic<*> {
        var baseDynamic = dynamic
        val oldIVs = baseDynamic.get(DataKeys.POKEMON_IVS).get().result().getOrNull() ?: return baseDynamic
        if (oldIVs.get(DataKeys.POKEMON_IVS_HYPERTRAINED).result().isPresent) {
            // It's already run
            return baseDynamic
        }
        baseDynamic = baseDynamic.remove(DataKeys.POKEMON_IVS)
        val newIVs = baseDynamic.createMap(mapOf(baseDynamic.createString(DataKeys.POKEMON_IVS_BASE) to oldIVs,
            baseDynamic.createString(DataKeys.POKEMON_IVS_HYPERTRAINED) to baseDynamic.createMap(emptyMap())))
        baseDynamic = baseDynamic.set(DataKeys.POKEMON_IVS, newIVs)
        return baseDynamic
    }
}