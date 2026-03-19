/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.partyproviders

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.npc.NPCPartyProvider
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.storage.party.NPCPartyStore
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.asArrayValue
import com.cobblemon.mod.common.util.getBooleanOrNull
import com.cobblemon.mod.common.util.toProperties
import com.google.gson.JsonElement
import net.minecraft.server.level.ServerPlayer

/**
 * A basic party provider that just produces a [NPCPartyStore] based on a simple list of [PokemonProperties].
 *
 * @author Hiroku
 * @since August 19th, 2023
 */
class SimplePartyProvider : NPCPartyProvider {
    companion object {
        const val TYPE = "simple"
    }

    @Transient
    override val type = TYPE
    override var isStatic = true

    val pokemon = mutableListOf<PokemonProperties>()

    @Transient
    val struct = ObjectValue(this).also {
        it.addFunction("add_pokemon") { params ->
            if (pokemon.size >= 6) {
                return@addFunction DoubleValue.ZERO
            }
            val pokemon = params.getString(0).toProperties()
            this.pokemon.add(pokemon)
            DoubleValue.ONE
        }
        it.addFunction("clear_pokemon") { params ->
            this.pokemon.clear()
            DoubleValue.ONE
        }
        it.addFunction("set_static") { params ->
            this.isStatic = params.getBooleanOrNull(0) != false
            DoubleValue.ONE
        }
        it.addFunction("static") { params ->
            DoubleValue(isStatic)
        }
        it.addFunction("pokemon") { params ->
            pokemon.asArrayValue { it.asMoLangValue() }
        }
    }

    override fun loadFromJSON(json: JsonElement) {
        val isStatic = json.asJsonObject.get("isStatic")?.asBoolean
        if (isStatic != null) {
            this.isStatic = isStatic
        }
        val pokemonList = json.asJsonObject.getAsJsonArray("pokemon")
        pokemonList.forEach {
            val pokemon = if (it.isJsonPrimitive) {
                it.asString.toProperties()
            } else {
                val obj = it.asJsonObject
                val props = PokemonProperties()
                props.loadFromJSON(obj)
                props
            }

            this.pokemon.add(pokemon)
        }
    }

    override fun provide(npc: NPCEntity, level: Int, players: List<ServerPlayer>): NPCPartyStore {
        val pokemon = pokemon.map { it.copy().also { it.level = it.level ?: level }.create() }
        val party = NPCPartyStore(npc)
        pokemon.forEach(party::add)
        return party
    }
}