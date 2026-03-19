/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.api.mark.Mark
import com.cobblemon.mod.common.api.mark.Marks
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf

class MarksPotentialUpdatePacket(pokemon: () -> Pokemon?, value: MutableSet<Mark>): SingleUpdatePacket<MutableSet<Mark>, MarksPotentialUpdatePacket>(pokemon, value) {
    override val id = ID
    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(this.value) { pb, value -> pb.writeIdentifier(value.identifier) }
    }

    override fun set(pokemon: Pokemon, value: MutableSet<Mark>) {
        pokemon.potentialMarks = value
    }

    companion object {
        val ID = cobblemonResource("potential_marks_update")
        fun decode(buffer: RegistryFriendlyByteBuf): MarksPotentialUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val identifiers = buffer.readList(ByteBuf::readIdentifier).toList()
            val potentialMarks = identifiers.map { Marks.getByIdentifier(it) }.filterNotNull().toMutableSet()
            return MarksPotentialUpdatePacket(pokemon, potentialMarks)
        }
    }
}
