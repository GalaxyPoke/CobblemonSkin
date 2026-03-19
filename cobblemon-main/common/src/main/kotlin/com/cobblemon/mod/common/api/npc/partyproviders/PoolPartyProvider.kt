/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.partyproviders
import com.bedrockk.molang.Expression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.npc.NPCPartyProvider
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.storage.party.NPCPartyStore
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.minecraft.server.level.ServerPlayer
import kotlin.collections.List
import kotlin.collections.any
import kotlin.collections.filter
import kotlin.collections.first
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.randomOrNull
import kotlin.collections.set
import kotlin.collections.toMutableList
import kotlin.random.Random

/**
 * A provider of a party for battling the NPC. It generates a party using a more complex process
 * that takes an input level and makes a randomized selection of Pokémon and movesets from a pool.
 *
 * The party can be static or dynamic based on [isStatic]. If it is dynamic, the pool will be used
 * to generate a party whenever the NPC is challenged and AI that depends on a reliable party will
 * not run.
 *
 * Not an actual pool party. That's for v2.0.
 *
 * @author Hiroku
 * @since July 12th, 2024
 */
class PoolPartyProvider : NPCPartyProvider {
    companion object {
        const val TYPE = "pool"
    }

    @Transient
    override val type = TYPE

    override var isStatic: Boolean = false
    var useFixedRandom: Boolean = false
    var minPokemon: Expression = "1".asExpression()
    var maxPokemon: Expression = "6".asExpression()
    var pool = mutableListOf<DynamicPokemon>()

    override fun loadFromJSON(json: JsonElement) {
        json as JsonObject
        this.minPokemon = json.getAsJsonPrimitive("minPokemon").asString?.asExpression() ?: "1".asExpression()
        this.maxPokemon = json.getAsJsonPrimitive("maxPokemon").asString?.asExpression() ?: "6".asExpression()
        this.pool = json.getAsJsonArray("pool").mapNotNull {
            if (it is JsonPrimitive) {
                return@mapNotNull DynamicPokemon(
                    PokemonProperties.parse(it.asString),
                    "0".asExpression(),
                    null,
                    1..100,
                    "1".asExpression(),
                    "1".asExpression()
                )
            }

            it as JsonObject
            val properties = PokemonProperties.parse(it.get("pokemon").asString)
                // Checks for errors on Pokémon name, if not a valid species ignores it to avoid random Pokémon in the Pool.
            if (properties.species == null) {
                LOGGER.warn("Error making NPC pokemon pool. ${it.get("pokemon").asString.substringBefore(" ")} is not a valid species, ignoring")
                    null
            }
            else {
                DynamicPokemon(
                    properties,
                    it.get("levelVariation")?.asString?.asExpression() ?: "0".asExpression(),
                    it.get("level")?.asString?.asExpression(),
                    it.get("npcLevels")?.asString?.split("-")?.let { it[0].toInt()..it[1].toInt() } ?: 1..100,
                    it.get("selectableTimes")?.asString?.asExpression() ?: "1".asExpression(),
                    it.get("weight")?.asString?.asExpression() ?: "1".asExpression()
                )
            }
        }.toMutableList()
        isStatic = json.get("isStatic").asBoolean
        json.get("useFixedRandom")?.asBoolean?.let { useFixedRandom = it }

    }

    class DynamicPokemon(
        val pokemon: PokemonProperties,
        val levelVariation: Expression,
        val level: Expression? = null,
        val npcLevels: IntRange,
        val selectableTimes: Expression,
        val weight: Expression = "1".asExpression()
    ) {
        fun getWeight(runtime: MoLangRuntime): Float {
            return runtime.resolveFloat(weight)
        }

        fun hasWeight(runtime: MoLangRuntime): Boolean {
            return runtime.resolveFloat(weight) != 0F
        }
    }

    fun formulateParty(npc: NPCEntity, level: Int, players: List<ServerPlayer>, party: NPCPartyStore) {
        val runtime = MoLangRuntime().setup().withQueryValue("npc", npc.struct)
        val random = if (useFixedRandom) Random(npc.uuid.hashCode()) else Random.Default
        runtime.withQueryValue("level", DoubleValue(level))
        runtime.withQueryValue("players", players.asArrayValue { it.asMoLangValue() })
        if (players.size == 1) {
            // This is for the convenience, most cases will be pvn one player
            runtime.withQueryValue("player", players.first().asMoLangValue())
        }
        val minPokemon = runtime.resolveInt(this.minPokemon)
        val maxPokemon = runtime.resolveInt(this.maxPokemon)
        var desiredPokemonCount = (minPokemon..maxPokemon).random(random)

        val workingPool = pool.filter { level in it.npcLevels && runtime.resolveInt(it.selectableTimes) > 0 }.toMutableList()
        val useCounts = mutableMapOf<DynamicPokemon, Int>()

        while (desiredPokemonCount > 0 && workingPool.any { it.hasWeight(runtime) }) {
            val selected = workingPool.filter { it.getWeight(runtime) == -1F }.randomOrNull(random)
                ?: workingPool.weightedSelection(random = random) { it.getWeight(runtime) }
                ?: break
            useCounts[selected] = useCounts.getOrDefault(selected, 0) + 1
            val allowedSelections = runtime.resolveInt(selected.selectableTimes)
            if (useCounts[selected]!! >= allowedSelections) {
                workingPool.remove(selected)
            }
            desiredPokemonCount--

            // If the Pokémon's props specifies a level then use that, otherwise choose a random level within the range
            val levelVariation = (0..runtime.resolveInt(selected.levelVariation)).random(random)
            val randomLevel = level + levelVariation
            val dictatedLevel = selected.level?.let { runtime.resolveInt(it) }
            val instance = selected.pokemon.copy().also { it.level = it.level ?: dictatedLevel ?: randomLevel }.create()
            party.add(instance)
        }
        if (party.none()) {
            LOGGER.error("${npc} has no Pokemon on Party")
        }

    }

    override fun provide(npc: NPCEntity, level: Int, players: List<ServerPlayer>): NPCPartyStore {
        val party = NPCPartyStore(npc)
        formulateParty(npc, level, players, party)
        return party
    }
}