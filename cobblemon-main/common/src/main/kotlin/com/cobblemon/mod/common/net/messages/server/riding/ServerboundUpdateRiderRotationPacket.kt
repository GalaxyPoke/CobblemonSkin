/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.riding

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.phys.Vec3

class ServerboundUpdateRiderRotationPacket internal constructor(
    val riderXRot: Float,
    val riderYRot: Float,
    val rideEyePos: Vec3
) : NetworkPacket<ServerboundUpdateRiderRotationPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeFloat(riderXRot)
        buffer.writeFloat(riderYRot)
        buffer.writeVec3(rideEyePos)
    }

    companion object {
        val ID = cobblemonResource("c2s_update_rider_rotation")
        fun decode(buffer: RegistryFriendlyByteBuf): ServerboundUpdateRiderRotationPacket {
            val riderXRot = buffer.readFloat()
            val riderYRot = buffer.readFloat()
            val rideEyePos = buffer.readVec3()
            return ServerboundUpdateRiderRotationPacket(riderXRot, riderYRot, rideEyePos)
        }
    }
}
