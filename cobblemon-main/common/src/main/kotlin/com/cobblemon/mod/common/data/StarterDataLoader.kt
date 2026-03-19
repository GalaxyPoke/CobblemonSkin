/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.data

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.config.starter.StarterCategory
import com.cobblemon.mod.common.util.adapters.pokemonPropertiesShortAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType

object StarterDataLoader : JsonDataRegistry<StarterCategory> {

    override val id: ResourceLocation = cobblemonResource("starters")
    override val type: PackType = PackType.SERVER_DATA
    override val observable = SimpleObservable<StarterDataLoader>()

    override val gson = GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .registerTypeAdapter(PokemonProperties::class.java, pokemonPropertiesShortAdapter)
        .create()

    override val typeToken: TypeToken<StarterCategory> = TypeToken.get(StarterCategory::class.java)
    override val resourcePath: String = "starters"

    private val categories = mutableListOf<StarterCategory>()
    fun getAllCategories(): List<StarterCategory> = categories.toList()

    override fun reload(data: Map<ResourceLocation, StarterCategory>) {
        categories.clear()

        // If enabled, start with default built-in starters
        if (Cobblemon.starterConfig.useConfigStarters) {
            categories += Cobblemon.starterConfig.starters
        }

        // Validate and collect only the valid categories
        val loadedCategories = data.mapNotNull { (id, category) ->
            if (category.name.isNullOrBlank()) {
                LOGGER.warn("Skipping starter category '{}': missing name", id)
                return@mapNotNull null
            }
            val name = category.name.trim()
            val displayName = category.displayName.takeIf { !it.isNullOrBlank() } ?: name

            val pokemonList = category.pokemon
            if (pokemonList.isNullOrEmpty()) {
                LOGGER.warn("Skipping starter category '{}': pokemon list is empty", id)
                return@mapNotNull null
            }
            StarterCategory(name, displayName, pokemonList)
        }

        // Default: If datapack exist then only use those, otherwise fall back to built-in starters
        if (loadedCategories.isNotEmpty() && !Cobblemon.starterConfig.useConfigStarters) {
            categories.clear()
            categories += loadedCategories
        } else {
            // Merge: Replace matching entries in-place, otherwise append
            for (newCategory in loadedCategories) {
                val existingIndex = categories.indexOfFirst {
                    it.name.equals(newCategory.name, ignoreCase = true)
                }
                if (existingIndex >= 0) {
                    categories[existingIndex] = newCategory
                    LOGGER.info("Replaced starter category '{}' at position {}", newCategory.name, existingIndex)
                } else {
                    categories += newCategory
                    LOGGER.info("Appended starter category '{}'", newCategory.name)
                }
            }
        }

        observable.emit(this)
    }

    override fun sync(player: ServerPlayer) {}
}