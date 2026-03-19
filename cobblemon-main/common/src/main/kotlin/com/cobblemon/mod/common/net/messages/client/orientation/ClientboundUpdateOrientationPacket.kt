/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.orientation

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readMatrix3f
import com.cobblemon.mod.common.util.writeMatrix3f
import net.minecraft.network.RegistryFriendlyByteBuf
import org.joml.Matrix3f

/**
 * Packet sent from the server to update the current orientation of the
 * given entity for the remote entity receiving this packet.
 *
 * @author Jackowes
 * @since March 30th, 2025
 */

class ClientboundUpdateOrientationPacket internal constructor(val orientation: Matrix3f?, val active: Boolean?, val entityId: Int) : NetworkPacket<ClientboundUpdateOrientationPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeVarInt(entityId)
        buffer.writeBoolean(orientation != null)
        if (orientation != null && active != null) {
            buffer.writeMatrix3f(orientation)
            buffer.writeBoolean(active)
        }
    }

    companion object {
        val ID = cobblemonResource("s2c_update_orientation")
        fun decode(buffer: RegistryFriendlyByteBuf): ClientboundUpdateOrientationPacket {
            val entityId = buffer.readVarInt()
            val valid = buffer.readBoolean()
            val orientation = if (valid) buffer.readMatrix3f() else null
            val active = if (valid) buffer.readBoolean() else null
            return ClientboundUpdateOrientationPacket(orientation, active, entityId)
        }
    }
}
