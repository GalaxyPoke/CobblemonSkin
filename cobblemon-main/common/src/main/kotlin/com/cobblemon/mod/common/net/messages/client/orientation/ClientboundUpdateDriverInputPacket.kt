/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.orientation

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.ClientboundUpdateRidingStatePacket.Companion.ID
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readMatrix3f
import com.cobblemon.mod.common.util.writeMatrix3f
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.player.Player
import org.joml.Matrix3f
import org.joml.Vector3f

/**
 * Packet sent from the server to update the current input from
 * the driver of a ridden pokemon. This data is needed for client
 * side animation data updates found in RidingAnimationData
 *
 * @author Jackowes
 * @since August 1st, 2025
 */

class ClientboundUpdateDriverInputPacket internal constructor(
    val driverInput: Vector3f,
    val entityId: Int
) : NetworkPacket<ClientboundUpdateDriverInputPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeVarInt(entityId)
        buffer.writeVector3f(driverInput)
    }

    companion object {
        val ID = cobblemonResource("s2c_update_driver_input")
        fun decode(buffer: RegistryFriendlyByteBuf): ClientboundUpdateDriverInputPacket {
            val entityId = buffer.readVarInt()
            val driverInput = buffer.readVector3f()
            return ClientboundUpdateDriverInputPacket(driverInput, entityId)
        }
    }
}
