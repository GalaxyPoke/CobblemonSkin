/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.item.ItemStack

/**
 * A Pokémon that can absolutely, under many circumstances, be rendered (or else!!!).
 *
 * @author Hiroku
 * @since August 1st, 2022
 */
data class RenderablePokemon(var species: Species, var aspects: Set<String>, var heldItem: ItemStack = ItemStack.EMPTY) {
    val form: FormData by lazy { species.getForm(aspects) }

    fun saveToBuffer(buffer: RegistryFriendlyByteBuf): RegistryFriendlyByteBuf {
        buffer.writeIdentifier(species.resourceIdentifier)
        buffer.writeSizedInt(IntSize.U_BYTE, aspects.size)
        aspects.forEach(buffer::writeString)
        buffer.writeItemStack(heldItem)
        return buffer
    }

    override fun equals(other: Any?): Boolean {
        return if (other is RenderablePokemon) (
            other.species.resourceIdentifier == this.species.resourceIdentifier &&
            other.aspects == this.aspects &&
            other.heldItem == this.heldItem
        ) else false
    }

    companion object {
        fun loadFromBuffer(buffer: RegistryFriendlyByteBuf): RenderablePokemon {
            val species = PokemonSpecies.getByIdentifier(buffer.readIdentifier())!!
            val aspects = mutableSetOf<String>()
            repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
                aspects.add(buffer.readString())
            }
            val heldItem = buffer.readItemStack()
            return RenderablePokemon(species, aspects, heldItem)
        }
    }
}