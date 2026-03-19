/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon

import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.moLangFunctionMap
import com.cobblemon.mod.common.api.pokedex.AbstractPokedexManager
import com.cobblemon.mod.common.api.pokedex.FormDexRecord
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.pokedex.scanner.PokedexEntityData
import com.cobblemon.mod.common.util.getPlayer
import java.util.UUID

/**
 * Event that fires when a Pokémon's information is gained or updated in the Pokédex.
 */
sealed interface PokedexDataChangedEvent {
    val dataSource: PokedexEntityData
    val knowledge: PokedexEntryProgress
    val playerUUID: UUID
    val record: FormDexRecord

    val pokedexManager: AbstractPokedexManager
        get() = record.speciesDexRecord.pokedexManager

    fun getContext(): MutableMap<String, MoValue> {
        return mutableMapOf(
            "player" to (playerUUID.getPlayer()?.asMoLangValue() ?: DoubleValue.ZERO),
            "pokemon" to dataSource.pokemon.struct,
            "disguise" to (dataSource.disguise?.struct ?: DoubleValue.ZERO),
            "knowledge" to StringValue(knowledge.name.lowercase()),
            "pokedex" to pokedexManager.struct
        )
    }

    class Pre(
        override val dataSource: PokedexEntityData,
        override val knowledge: PokedexEntryProgress,
        override val playerUUID: UUID,
        override val record: FormDexRecord
    ) : PokedexDataChangedEvent, Cancelable() {
        val functions = moLangFunctionMap(cancelFunc)
    }

    class Post(
        override val dataSource: PokedexEntityData,
        override val knowledge: PokedexEntryProgress,
        override val playerUUID: UUID,
        override val record: FormDexRecord
    ) : PokedexDataChangedEvent
}
