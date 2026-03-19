/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.abilities

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.battles.runner.ShowdownService
import com.cobblemon.mod.common.net.messages.client.data.AbilityRegistrySyncPacket
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import java.io.File
import kotlin.collections.set

/**
 * Registry for all known Abilities
 */
object Abilities : DataRegistry {

    override val id = cobblemonResource("abilities")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<Abilities>()

    val DUMMY = AbilityTemplate(name = "dummy")

    private val abilityMap = mutableMapOf<String, AbilityTemplate>()
    internal val abilityScripts = mutableMapOf<String, String>() // abilityId to JavaScript

    override fun reload(manager: ResourceManager) {
        PotentialAbility.types.clear()
        PotentialAbility.types.add(CommonAbilityType)
        PotentialAbility.types.add(HiddenAbilityType)
        this.abilityMap.clear()
        this.abilityScripts.clear()

        ShowdownService.service.resetRegistryData("ability")
        manager.listResources("abilities") { it.path.endsWith(".js") }.forEach { (identifier, resource) ->
            resource.open().use { stream ->
                stream.bufferedReader().use { reader ->
                    val resolvedIdentifier = ResourceLocation.fromNamespaceAndPath(identifier.namespace, File(identifier.path).nameWithoutExtension)
                    val js = reader.readText()
                    abilityScripts[resolvedIdentifier.path] = js
                }
            }
        }
        ShowdownService.service.sendRegistryData(abilityScripts, "ability")

        val abilitiesJson = ShowdownService.service.getRegistryData("ability")
        for (i in 0 until abilitiesJson.size()) {
            val jsonAbility = abilitiesJson[i].asJsonObject
            val id = jsonAbility.get("id").asString
            val ability = AbilityTemplate(id)
            this.register(ability)
        }
        Cobblemon.LOGGER.info("Loaded {} abilities", this.abilityMap.size)
        this.observable.emit(this)
    }

    override fun sync(player: ServerPlayer) {
        AbilityRegistrySyncPacket(all()).sendToPlayer(player)
    }

    @JvmStatic
    fun register(ability: AbilityTemplate): AbilityTemplate {
        this.abilityMap[ability.name.lowercase()] = ability
        return ability
    }

    @JvmStatic
    fun all() = this.abilityMap.values.toList()
    @JvmStatic
    fun first() = this.abilityMap.values.first()
    @JvmStatic
    fun get(name: String) = abilityMap[name.lowercase()]
    @JvmStatic
    fun getOrDummy(name: String) = get(name) ?: DUMMY
    @JvmStatic
    fun getOrException(name: String) = get(name) ?: throw IllegalArgumentException("Unable to find ability of name: $name")
    @JvmStatic
    fun count() = this.abilityMap.size

    internal fun receiveSyncPacket(abilities: Collection<AbilityTemplate>) {
        this.abilityMap.clear()
        abilities.forEach { ability -> this.register(ability) }
    }

}