/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex.filter

import com.cobblemon.mod.common.api.drop.ItemDropEntry
import com.cobblemon.mod.common.api.pokedex.AbstractPokedexManager
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.itemRegistry
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemStack

/**
 * A Pokedex [EntryFilter] that filters out entries that do not contain the current search.
 *
 * @author whatsy
 * @since September 4th, 2024
 * @param searchString The string to use when checking.
 */
class SearchFilter(val pokedexManager: AbstractPokedexManager, val searchString: String, val searchByType: SearchByType = SearchByType.SPECIES) : EntryFilter() {

    override fun test(entry: PokedexEntry): Boolean {
        if (searchString == "") return true

        val species = PokemonSpecies.getByIdentifier(entry.speciesId) ?: return false
        val highestKnowledgeForEntry = pokedexManager.getHighestKnowledgeFor(entry)
        if (highestKnowledgeForEntry == PokedexEntryProgress.NONE) return false

        when (searchByType) {
            SearchByType.ABILITIES -> {
                if (pokedexManager.getHighestKnowledgeFor(entry) !== PokedexEntryProgress.CAUGHT) return false
                val abilityList = mutableListOf<String>()
                val formsList = if (species.forms.isEmpty()) mutableListOf(species.standardForm) else species.forms
                formsList.forEach {
                    it.abilities.sortedBy { it is HiddenAbility }.map { ability -> ability.template }.forEach {
                        abilityList.add(it.displayName.asTranslated().string.lowercase())
                    }
                }
                return abilityList.any { it.contains(searchString.trim().lowercase()) }
            }
            SearchByType.DROPS -> {
                if (pokedexManager.getHighestKnowledgeFor(entry) !== PokedexEntryProgress.CAUGHT) return false
                val dropsList = mutableListOf<String>()
                val formsList = if (species.forms.isEmpty()) mutableListOf(species.standardForm) else species.forms
                formsList.forEach {
                    it.drops.entries.forEach {
                        if (it is ItemDropEntry) {
                            val itemStack = Minecraft.getInstance().player?.level()?.itemRegistry?.get(it.item)?.defaultInstance ?: ItemStack.EMPTY
                            if (!itemStack.isEmpty) dropsList.add(itemStack.displayName.string.lowercase())
                        }
                    }
                }
                return dropsList.any { it.contains(searchString.trim().lowercase()) }
            }
            // Search by species name
            else -> {
                return species.translatedName.string.contains(searchString.trim(), true)
            }
        }
    }

}