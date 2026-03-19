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
import net.minecraft.network.RegistryFriendlyByteBuf

class ActiveMarkUpdatePacket(pokemon: () -> Pokemon?, value: Mark?): SingleUpdatePacket<Mark?, ActiveMarkUpdatePacket>(pokemon, value) {
    override val id = ID
    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeNullable(this.value) { _, v -> buffer.writeIdentifier(v.identifier) }
    }

    override fun set(pokemon: Pokemon, value: Mark?) {
        pokemon.activeMark = value
    }

    companion object {
        val ID = cobblemonResource("active_mark_update")
        fun decode(buffer: RegistryFriendlyByteBuf): ActiveMarkUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val markIdentifier = buffer.readNullable { buffer.readIdentifier() }
            val mark = if (markIdentifier == null) null else Marks.getByIdentifier(markIdentifier)
            return ActiveMarkUpdatePacket(pokemon, mark)
        }
    }
}
