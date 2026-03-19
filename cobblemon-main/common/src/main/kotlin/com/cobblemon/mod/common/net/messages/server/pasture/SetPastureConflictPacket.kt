/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.pasture

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf

class SetPastureConflictPacket(val pokemonId: UUID, val enabled: Boolean) : NetworkPacket<SetPastureConflictPacket> {
    companion object {
        val ID = cobblemonResource("set_pasture_conflict")
        fun decode(buf: RegistryFriendlyByteBuf) =
            SetPastureConflictPacket(buf.readUUID(), buf.readBoolean())
    }

    override val id = ID

    override fun encode(buf: RegistryFriendlyByteBuf) {
        buf.writeUUID(pokemonId)
        buf.writeBoolean(enabled)
    }
}
