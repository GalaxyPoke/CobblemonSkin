/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.pokemon.update

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourState
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.server
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class ServerboundUpdateRidingStatePacket(
    val entity: Int,
    val behaviour: ResourceLocation,
    val state: RidingBehaviourState? = null,
    val data: RegistryFriendlyByteBuf? = null
) : NetworkPacket<ServerboundUpdateRidingStatePacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        if (state == null) error("Expected state to be populated for encoding")
        buffer.writeInt(entity)
        buffer.writeResourceLocation(behaviour)
        state.encode(buffer)
    }

    companion object {
        val ID = cobblemonResource("c2s_update_ride_controller")
        fun decode(buffer: RegistryFriendlyByteBuf): ServerboundUpdateRidingStatePacket {
            val entity = buffer.readInt()
            val behaviour = buffer.readResourceLocation()
            val state = RegistryFriendlyByteBuf(buffer.readBytes(buffer.readableBytes()), server()!!.registryAccess())
            return ServerboundUpdateRidingStatePacket(
                entity = entity,
                behaviour = behaviour,
                data = state
            )
        }
    }
}
