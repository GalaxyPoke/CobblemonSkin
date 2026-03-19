/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.pokemon.update

import com.cobblemon.mod.common.api.mark.Mark
import com.cobblemon.mod.common.api.mark.Marks
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.serverhandling.pokemon.update.SetActiveMarkHandler
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

/**
 * Handled by [SetActiveMarkHandler].
 */
class SetActiveMarkPacket(val uuid: UUID, val mark: Mark?) : NetworkPacket<SetActiveMarkPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(uuid)
        buffer.writeNullable(mark) { _, v -> buffer.writeIdentifier(v.identifier) }
    }

    companion object {
        val ID = cobblemonResource("set_active_mark")
        fun decode(buffer: RegistryFriendlyByteBuf): SetActiveMarkPacket {
            val uuid = buffer.readUUID()
            val markIdentifier = buffer.readNullable { buffer.readIdentifier() }
            val mark = if (markIdentifier !== null) Marks.getByIdentifier(markIdentifier) else null
            return SetActiveMarkPacket(uuid, mark)
        }
    }
}
