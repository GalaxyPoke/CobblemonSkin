/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf

class MarkingsUpdatePacket(pokemon: () -> Pokemon?, value: List<Int>): SingleUpdatePacket<List<Int>, MarkingsUpdatePacket>(pokemon, value) {
    override val id = ID
    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(this.value) { pb, value -> pb.writeInt(value) }
    }

    override fun set(pokemon: Pokemon, value: List<Int>) {
        pokemon.markings = value
    }

    companion object {
        val ID = cobblemonResource("markings_update")
        fun decode(buffer: RegistryFriendlyByteBuf): MarkingsUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val markings = buffer.readList(ByteBuf::readInt).toList()
            return MarkingsUpdatePacket(pokemon, markings)
        }
    }
}
