/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.pc

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.pokemon.PokemonSortMode
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.storage.InvalidSpeciesException
import com.cobblemon.mod.common.api.storage.StoreCoordinates
import com.cobblemon.mod.common.net.messages.client.storage.pc.SetPCBoxPacket
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.JsonObject
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * A single box of a PC. The list of Pokémon is strictly sized at [POKEMON_PER_BOX] - 30.
 * Any change to any contained Pokémon is emitted through the [boxChangeEmitter].
 *
 * @author Hiroku
 * @since April 26th, 2022
 */
open class PCBox(val pc: PCStore) : Iterable<Pokemon> {
    override fun iterator() = pokemon.filterNotNull().iterator()

    val boxChangeEmitter = SimpleObservable<Unit>()

    protected var emit = true

    var name : String? = null
        set(value) {
            field = value
            if (emit) boxChangeEmitter.emit(Unit)
        }

    var wallpaper : ResourceLocation = cobblemonResource("textures/gui/pc/pc_screen_overlay.png")
        set(value) {
            field = value
            if (emit) boxChangeEmitter.emit(Unit)
        }

    protected val pokemon = Array<Pokemon?>(POKEMON_PER_BOX) { null }

    open operator fun get(index: Int): Pokemon? {
        return if (index in 0 until POKEMON_PER_BOX) {
            pokemon[index]
        } else {
            null
        }
    }

    open operator fun set(index: Int, pokemon: Pokemon?) {
        if (index in 0 until POKEMON_PER_BOX) {
            this.pokemon[index] = pokemon
            pokemon?.storeCoordinates?.set(StoreCoordinates(pc, PCPosition(boxNumber, index)))
            if (emit) {
                boxChangeEmitter.emit(Unit)
            }
        }
    }

    val boxNumber: Int
        get() = this.pc.boxes.indexOf(this)

    val unoccupiedSlots: Int
        get() = POKEMON_PER_BOX - this.pokemon.filterNotNull().count()

    fun getFirstAvailablePosition(): PCPosition? {
        for (index in 0 until POKEMON_PER_BOX) {
            if (this.pokemon[index] == null) {
                return PCPosition(boxNumber, index)
            }
        }
        return null
    }

    open fun initialize() {
        val box = boxNumber
        pokemon.forEachIndexed { slot, pokemon ->
            if (pokemon != null) {
                val position = PCPosition(box, slot)
                pokemon.storeCoordinates.set(StoreCoordinates(pc, position))
            }
        }
        boxChangeEmitter.subscribe { pc.pcChangeObservable.emit(Unit) }
    }

    fun sort(sortMode: PokemonSortMode, descending: Boolean) {
        pokemon.sortWith(sortMode.comparator(descending))
        pokemon.forEachIndexed { slot, pokemon ->
            pokemon?.storeCoordinates?.set(StoreCoordinates(pc, PCPosition(boxNumber, slot)))
        }
        boxChangeEmitter.emit(Unit)
    }

    fun sendTo(player: ServerPlayer) {
        SetPCBoxPacket(this).sendToPlayer(player)
    }

    open fun saveToNBT(nbt: CompoundTag, registryAccess: RegistryAccess): CompoundTag {
        name?.let {
            nbt.putString(DataKeys.STORE_BOX_NAME, it)
        }
        nbt.putString(DataKeys.STORE_BOX_WALLPAPER, wallpaper.toString())
        for (slot in 0 until POKEMON_PER_BOX) {
            val pokemon = pokemon[slot] ?: continue
            nbt.put(DataKeys.STORE_SLOT + slot, pokemon.saveToNBT(registryAccess))
        }
        return nbt
    }

    open fun saveToJSON(json: JsonObject, registryAccess: RegistryAccess): JsonObject {
        name?.let {
            json.addProperty(DataKeys.STORE_BOX_NAME, it)
        }
        json.addProperty(DataKeys.STORE_BOX_WALLPAPER, wallpaper.toString())
        for (slot in 0 until POKEMON_PER_BOX) {
            val pokemon = pokemon[slot] ?: continue
            json.add(DataKeys.STORE_SLOT + slot, pokemon.saveToJSON(registryAccess))
        }
        return json
    }


    open fun loadFromJSON(json: JsonObject, registryAccess: RegistryAccess): PCBox {
        if (json.has(DataKeys.STORE_BOX_NAME)) {
            name = json.getAsJsonPrimitive(DataKeys.STORE_BOX_NAME).asString
        }

        if (json.has(DataKeys.STORE_BOX_WALLPAPER)) {
            wallpaper = ResourceLocation.parse(json.getAsJsonPrimitive(DataKeys.STORE_BOX_WALLPAPER).asString)
        }

        for (slot in 0 until POKEMON_PER_BOX) {
            if (json.has(DataKeys.STORE_SLOT + slot)) {
                val pokemonJson = json.getAsJsonObject(DataKeys.STORE_SLOT + slot)
                try {
                    pokemon[slot] = Pokemon.loadFromJSON(registryAccess, pokemonJson)
                } catch (_: InvalidSpeciesException) {
                    pc.handleInvalidSpeciesJSON(pokemonJson)
                } catch (e: Exception) {
                    LOGGER.error("Failed to read a pokémon: $pokemonJson", e)
                }
            }
        }
        return this
    }

    open fun loadFromNBT(nbt: CompoundTag, registryAccess: RegistryAccess): PCBox {
        if (nbt.contains(DataKeys.STORE_BOX_NAME)) {
            name = nbt.getString(DataKeys.STORE_BOX_NAME)
        }

        if (nbt.contains(DataKeys.STORE_BOX_WALLPAPER)) {
            wallpaper = ResourceLocation.parse(nbt.getString(DataKeys.STORE_BOX_WALLPAPER))
        }

        for (slot in 0 until POKEMON_PER_BOX) {
            if (nbt.contains(DataKeys.STORE_SLOT + slot)) {
                val pokemonNBT = nbt.getCompound(DataKeys.STORE_SLOT + slot)
                try {
                    pokemon[slot] = Pokemon.loadFromNBT(registryAccess, pokemonNBT)
                } catch (_: InvalidSpeciesException) {
                    pc.handleInvalidSpeciesNBT(pokemonNBT)
                } catch (e: Exception) {
                    LOGGER.error("Failed to read a pokémon: $pokemonNBT", e)
                }
            }
        }
        return this
    }

    open fun getNonEmptySlots() = (0 until POKEMON_PER_BOX).filter { get(it) != null }.associateWith { get(it)!! }
    open fun getNonEmptySlotsForPackets() = getNonEmptySlots().mapValues { (_, pokemon) -> { _: RegistryAccess -> pokemon } }
}