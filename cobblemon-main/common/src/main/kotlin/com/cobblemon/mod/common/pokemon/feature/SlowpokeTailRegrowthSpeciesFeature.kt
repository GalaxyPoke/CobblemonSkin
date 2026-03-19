/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.feature

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.CobblemonMechanics
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.aspect.AspectProvider
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatureProvider
import com.cobblemon.mod.common.api.pokemon.feature.TickingSpeciesFeature
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty
import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.isInt
import com.cobblemon.mod.common.util.jitterDropItem
import com.google.gson.JsonObject
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel

class SlowpokeTailRegrowthSpeciesFeature(var regrowthSeconds: Int = 0) : SpeciesFeature, CustomPokemonProperty, TickingSpeciesFeature {
    override val name: String = NAME

    override fun saveToNBT(pokemonNBT: CompoundTag): CompoundTag {
        if (regrowthSeconds > 0) {
            pokemonNBT.putInt(DataKeys.TAIL_REGROWTH_SECONDS, regrowthSeconds)
        }
        return pokemonNBT
    }

    override fun loadFromNBT(pokemonNBT: CompoundTag): SpeciesFeature {
        regrowthSeconds = pokemonNBT.getInt(DataKeys.TAIL_REGROWTH_SECONDS)
        return this
    }

    override fun saveToJSON(pokemonJSON: JsonObject): JsonObject {
        if (regrowthSeconds > 0) {
            pokemonJSON.addProperty(DataKeys.TAIL_REGROWTH_SECONDS, regrowthSeconds)
        }
        return pokemonJSON
    }

    override fun loadFromJSON(pokemonJSON: JsonObject): SpeciesFeature {
        if (pokemonJSON.has(DataKeys.TAIL_REGROWTH_SECONDS)) {
            regrowthSeconds = pokemonJSON.get(DataKeys.TAIL_REGROWTH_SECONDS).asInt
        }
        return this
    }

    override fun asString(): String {
        return "$name=$regrowthSeconds"
    }

    override fun apply(pokemon: Pokemon) {
        pokemon.features.removeIf { it.name == NAME }
        pokemon.features.add(this)
        pokemon.updateAspects()
    }

    override fun matches(pokemon: Pokemon): Boolean {
        return (pokemon.getFeature(NAME) as? SlowpokeTailRegrowthSpeciesFeature)?.regrowthSeconds == regrowthSeconds
    }

    fun onShear(pokemonEntity: PokemonEntity) {
        this.regrowthSeconds = CobblemonMechanics.slowpokeTails.regrowthSeconds
        val itemEntity = pokemonEntity.spawnAtLocation(CobblemonItems.TASTY_TAIL) ?: return
        pokemonEntity.pokemon.updateAspects()
        pokemonEntity.pokemon.markFeatureDirty(this)
        pokemonEntity.jitterDropItem(itemEntity)
    }

    override fun onSecondPassed(
        world: ServerLevel,
        pokemon: Pokemon,
        entity: PokemonEntity?
    ) {
        if (regrowthSeconds <= 0) return
        if (CobblemonMechanics.slowpokeTails.onlyRegrowWhenSentOut && entity == null) return
        // if they're sent out and parameter entity is null, the party ticker is running this - leave it for the entity delegate ticker
        if (entity == null && pokemon.entity != null) return

        regrowthSeconds--
        pokemon.updateAspects()
        pokemon.markFeatureDirty(this)
    }

    companion object {
        const val NAME = "slowpoke_tail_regrowth"
    }
}

object SlowpokeTailRegrowthSpeciesFeatureProvider: SpeciesFeatureProvider<SlowpokeTailRegrowthSpeciesFeature>, CustomPokemonPropertyType<SlowpokeTailRegrowthSpeciesFeature>, AspectProvider {
    override val keys: Iterable<String> = setOf(SlowpokeTailRegrowthSpeciesFeature.NAME)
    override val needsKey: Boolean = true

    fun getFromPokemon(pokemon: Pokemon): SlowpokeTailRegrowthSpeciesFeature? {
        if (!CobblemonMechanics.slowpokeTails.canShearSlowpoke) {
            return null
        }
        return pokemon.features.find { it.name == SlowpokeTailRegrowthSpeciesFeature.NAME }
            ?.let { return it as SlowpokeTailRegrowthSpeciesFeature }
    }

    override fun invoke(pokemon: Pokemon): SlowpokeTailRegrowthSpeciesFeature {
        return getFromPokemon(pokemon)
            ?: SlowpokeTailRegrowthSpeciesFeature()
    }

    override fun invoke(nbt: CompoundTag): SlowpokeTailRegrowthSpeciesFeature? {
        return if (nbt.contains(DataKeys.TAIL_REGROWTH_SECONDS)) {
            SlowpokeTailRegrowthSpeciesFeature().also { it.loadFromNBT(nbt) }
        } else null
    }

    override fun invoke(json: JsonObject): SlowpokeTailRegrowthSpeciesFeature? {
        return if (json.has(DataKeys.TAIL_REGROWTH_SECONDS)) {
            SlowpokeTailRegrowthSpeciesFeature().also { it.loadFromJSON(json) }
        } else null
    }

    override fun fromString(value: String?): SlowpokeTailRegrowthSpeciesFeature? {
        val mechanic = CobblemonMechanics.slowpokeTails
        return if (value == null) {
            SlowpokeTailRegrowthSpeciesFeature(regrowthSeconds = mechanic.regrowthSeconds)
        } else if (value.isInt()) {
            SlowpokeTailRegrowthSpeciesFeature(regrowthSeconds = value.toInt())
        } else {
            null
        }
    }

    override fun examples() = listOf(CobblemonMechanics.slowpokeTails.regrowthSeconds.toString())

    override fun provide(pokemon: Pokemon): Set<String> {
        val mechanic = CobblemonMechanics.slowpokeTails
        val regrowthSeconds = pokemon.getFeature<SlowpokeTailRegrowthSpeciesFeature>(SlowpokeTailRegrowthSpeciesFeature.NAME)?.regrowthSeconds ?: return emptySet()
        return mechanic.getAspects(regrowthSeconds)
    }

    override fun provide(properties: PokemonProperties): Set<String> {
        val mechanic = CobblemonMechanics.slowpokeTails
        val regrowthSeconds = properties.customProperties.filterIsInstance<SlowpokeTailRegrowthSpeciesFeature>().firstOrNull()?.regrowthSeconds ?: return emptySet()
        return mechanic.getAspects(regrowthSeconds)
    }
}