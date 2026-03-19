/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.pokemon.update

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.serverhandling.pokemon.update.SetMarkingsHandler
import com.cobblemon.mod.common.util.*
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Handled by [SetMarkingsHandler].
 */
class SetMarkingsPacket(val uuid: UUID, val markings: List<Int>, val isParty: Boolean = true) : NetworkPacket<SetMarkingsPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(uuid)
        buffer.writeCollection(markings) { pb, value -> pb.writeInt(value) }
        buffer.writeBoolean(isParty)
    }

    companion object {
        val ID = cobblemonResource("set_markings")
        fun decode(buffer: RegistryFriendlyByteBuf): SetMarkingsPacket {
            val uuid = buffer.readUUID()
            val markings = buffer.readList(ByteBuf::readInt).toList()
            val isParty = buffer.readBoolean()
            return SetMarkingsPacket(uuid, markings, isParty)
        }
    }
}
