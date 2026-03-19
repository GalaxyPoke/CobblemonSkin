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
import com.cobblemon.mod.common.CobblemonBehaviours
import com.cobblemon.mod.common.CobblemonCallbacks
import com.cobblemon.mod.common.CobblemonCosmeticItems
import com.cobblemon.mod.common.CobblemonMechanics
import com.cobblemon.mod.common.CobblemonRideSettings
import com.cobblemon.mod.common.CobblemonUnlockableWallpapers
import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.berry.Berries
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.api.data.DataProvider
import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.dialogue.Dialogues
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.fishing.PokeRods
import com.cobblemon.mod.common.api.fishing.SpawnBait
import com.cobblemon.mod.common.api.fishing.SpawnBaitEffects
import com.cobblemon.mod.common.api.fossil.Fossils
import com.cobblemon.mod.common.api.fossil.NaturalMaterials
import com.cobblemon.mod.common.api.interaction.PokemonInteractions
import com.cobblemon.mod.common.api.item.HeldItems
import com.cobblemon.mod.common.api.mark.Marks
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.moves.animations.ActionEffects
import com.cobblemon.mod.common.api.npc.NPCClasses
import com.cobblemon.mod.common.api.npc.NPCPresets
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.pokedex.DexAdditions
import com.cobblemon.mod.common.api.pokedex.Dexes
import com.cobblemon.mod.common.api.pokedex.entry.DexEntries
import com.cobblemon.mod.common.api.pokedex.entry.DexEntryAdditions
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.feature.GlobalSpeciesFeatures
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatureAssignments
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures
import com.cobblemon.mod.common.api.scripting.CobblemonScripts
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnRules
import com.cobblemon.mod.common.api.spawning.SpawnDetailPresets
import com.cobblemon.mod.common.battles.BagItems
import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.cobblemon.mod.common.pokemon.SpeciesAdditions
import com.cobblemon.mod.common.pokemon.properties.PropertiesCompletionProvider
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.ifClient
import com.cobblemon.mod.common.util.server
import java.util.UUID
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener

object CobblemonDataProvider : DataProvider {

    // Both Forge n Fabric keep insertion order so if a registry depends on another simply register it after
    private val registries = linkedSetOf<DataRegistry>()
    private val reloadableRegistries = linkedSetOf<DataRegistry>()
    private val synchronizedPlayerIds = mutableListOf<UUID>()

    private val scheduledActions = mutableMapOf<UUID, MutableList<() -> Unit>>()

    fun registerDefaults() {
        this.register(CobblemonScripts, reloadable = true)
        this.register(SpeciesFeatures, reloadable = false)
        this.register(GlobalSpeciesFeatures, reloadable = false)
        this.register(SpeciesFeatureAssignments, reloadable = false)
        this.register(ActionEffects, reloadable = true)
        this.register(Moves, reloadable = false)
        this.register(Abilities, reloadable = false)
        this.register(CobblemonBehaviours, reloadable = false)
        this.register(PokemonSpecies, reloadable = false)
        this.register(SpeciesAdditions, reloadable = false)
        this.register(PokeBalls, reloadable = false)
        this.register(PropertiesCompletionProvider, reloadable = false)
        this.register(SpawnDetailPresets, reloadable = true)
        this.register(CobblemonSpawnRules, reloadable = true)
        this.register(CobblemonMechanics, reloadable = true)
        this.register(BagItems, reloadable = false)
        this.register(HeldItems, reloadable = false)
        this.register(Dialogues, reloadable = true)
        this.register(NaturalMaterials, reloadable = true)
        this.register(Fossils, reloadable = true)
        this.register(NPCPresets, reloadable = false)
        this.register(NPCClasses, reloadable = false)
        this.register(DexEntries, reloadable = false)
        this.register(DexEntryAdditions, reloadable = false)
        this.register(Dexes, reloadable = false)
        this.register(DexAdditions, reloadable = false)
        this.register(CobblemonCosmeticItems, reloadable = true)
        this.register(CobblemonCallbacks, reloadable = true)
        this.register(CobblemonUnlockableWallpapers, reloadable = true)
        this.register(Marks, reloadable = false)
        this.register(StarterDataLoader, reloadable = true)

        CobblemonSpawnPools.load()
        this.register(PokeRods, reloadable = false)
        this.register(Berries, reloadable = false)
        this.register(Seasonings, reloadable = false)
        this.register(PokemonInteractions, reloadable = false)
        this.register(SpawnBaitEffects, reloadable = false)
        this.register(CobblemonRideSettings, reloadable = true)
        SpawnBait.Effects.setupEffects()

        PlatformEvents.SERVER_PLAYER_LOGOUT.subscribe {
            synchronizedPlayerIds.remove(it.player.uuid)
        }

        ifClient {
            Cobblemon.implementation.registerResourceReloader(cobblemonResource("client_resources"), SimpleResourceReloader(PackType.CLIENT_RESOURCES), PackType.CLIENT_RESOURCES, emptyList())
        }
        Cobblemon.implementation.registerResourceReloader(cobblemonResource("data_resources"), SimpleResourceReloader(PackType.SERVER_DATA), PackType.SERVER_DATA, emptyList())
    }

    override fun <T : DataRegistry> register(registry: T, reloadable: Boolean): T {
        // Only send message once
        if (this.registries.isEmpty()) {
            LOGGER.info("Note: Cobblemon data registries are only loaded once per server instance as Pokémon species are not safe to reload.")
        }
        this.registries.add(registry)
        if (reloadable) {
            this.reloadableRegistries.add(registry)
        }
        LOGGER.info("Registered the {} registry", registry.id.toString())
        LOGGER.debug("Registered the {} registry of class {}", registry.id.toString(), registry::class.qualifiedName)
        return registry
    }

    override fun fromIdentifier(registryIdentifier: ResourceLocation): DataRegistry? = this.registries.find { it.id == registryIdentifier }

    override fun sync(player: ServerPlayer) {
        if (!player.connection.connection.isMemoryConnection) {
            this.registries.forEach { registry ->
                registry.sync(player)
            }
        }

        CobblemonEvents.DATA_SYNCHRONIZED.emit(player)
        val waitingActions = this.scheduledActions.remove(player.uuid) ?: return
        waitingActions.forEach { it() }
    }

    override fun doAfterSync(player: ServerPlayer, action: () -> Unit) {
        if (player.uuid in synchronizedPlayerIds) {
            action()
        } else {
            this.scheduledActions.computeIfAbsent(player.uuid) { mutableListOf() }.add(action)
        }
    }

    private class SimpleResourceReloader(private val type: PackType) : ResourceManagerReloadListener {
        override fun onResourceManagerReload(manager: ResourceManager) {
            // Check for a server running, this is due to the create a world screen triggering datapack reloads, these are fine to happen as many times as needed as players may be in the process of adding their datapacks.
            val reloadAllowed = server()?.isReady != true
            registries.filter { it.type == this.type && (reloadAllowed || it in reloadableRegistries) }
                .forEach { it.reload(manager) }
        }
    }
}
