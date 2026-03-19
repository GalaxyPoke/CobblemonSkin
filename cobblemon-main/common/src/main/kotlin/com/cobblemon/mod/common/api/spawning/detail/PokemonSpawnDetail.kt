/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.detail

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.drop.DropTable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.api.spawning.selection.SpawnSelectionData
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.weightedSelection
import com.google.gson.annotations.SerializedName
import kotlin.math.ceil
import net.minecraft.network.chat.MutableComponent

/**
 * A [SpawnDetail] for spawning a [PokemonEntity].
 *
 * @author Hiroku
 * @since February 13th, 2022
 */
class PokemonSpawnDetail : SpawnDetail() {
    companion object {
        val TYPE = "pokemon"
    }

    override val type: String = TYPE
    var pokemon = PokemonProperties()
    @SerializedName("level", alternate = ["levelRange"])
    var levelRange: IntRange? = null
    val drops: DropTable? = null
    val heldItems: MutableList<PossibleHeldItem>? = null

    private val pokemonExample: Pokemon by lazy { pokemon.create() }

    // Calculate the size based off the hitbox unless it's been explicitly set

    /* todo breadcrumbing, ai */


    override fun getName(): MutableComponent {
        displayName?.let { return it.asTranslated() }

        val speciesString = pokemon.species
        if (speciesString != null) {
            if (speciesString.lowercase() == "random") {
                return lang("species.random")
            }
            // ToDo exception handling
            val species = PokemonSpecies.getByIdentifier(speciesString.asIdentifierDefaultingNamespace())
            return if (species == null) {
                lang("species.unknown")
            } else {
                species.translatedName
            }
        } else {
            return lang("a_pokemon")
        }
    }

    override fun autoLabel() {
        struct.addFunction("pokemon") { pokemon.asMoLangValue() }
        struct.addFunction("min_level") { DoubleValue(pokemon.deriveLevelRange(levelRange).start.toDouble()) }
        struct.addFunction("max_level") { DoubleValue(pokemon.deriveLevelRange(levelRange).endInclusive.toDouble()) }
        if (pokemon.species != null) {
            val species = PokemonSpecies.getByIdentifier(pokemon.species!!.asIdentifierDefaultingNamespace())
            if (species != null) {
                labels.addAll(
                    species.secondaryType?.let { listOf(species.primaryType.showdownId, it.showdownId) }
                        ?: listOf(species.primaryType.showdownId)
                )

                if (height == -1) {
                    height = ceil(pokemonExample.form.hitbox.height * pokemonExample.form.baseScale).toInt()
                }

                if (width == -1) {
                    width = ceil(pokemonExample.form.hitbox.width * pokemonExample.form.baseScale).toInt()
                }
            }
        }

        super.autoLabel()
    }

    override fun isValid(): Boolean {
        val isValidSpecies = pokemon.species != null
        if (!isValidSpecies) LOGGER.error("Invalid species for spawn detail: $id")
        return super.isValid() && isValidSpecies
    }

    override fun createSpawnAction(
        spawnablePosition: SpawnablePosition, bucket: SpawnBucket,
        selectionData: SpawnSelectionData
    ): SingleEntitySpawnAction<PokemonEntity> {
        val levelRange = pokemon.deriveLevelRange(levelRange)
        val heldItems = heldItems?.takeIf { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
        val heldItem = if (heldItems.isNotEmpty()) {
            val until100 = 1 - heldItems.sumOf { it.percentage / 100 }
            if (until100 > 0 && spawnablePosition.world.random.nextDouble() < until100) {
                null
            } else {
                heldItems.weightedSelection { it.percentage }
            }
        } else {
            null
        }?.createStack(spawnablePosition)

        return PokemonSpawnAction(
            spawnablePosition = spawnablePosition,
            bucket = bucket,
            detail = this,
            props = pokemon,
            heldItem = heldItem,
            drops = drops,
            levelRange = levelRange
        )
    }
}