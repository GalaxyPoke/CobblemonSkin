/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.debug

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class ServerboundUpdateRidingStatsPacket(
    val entity: Int,
    val ridingStyle: RidingStyle,
    val speed: Double,
    val acceleration: Double,
    val skill: Double,
    val jump: Double,
    val stamina: Double,
) : NetworkPacket<ServerboundUpdateRidingStatsPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(entity)
        buffer.writeEnum(ridingStyle)
        buffer.writeDouble(speed)
        buffer.writeDouble(acceleration)
        buffer.writeDouble(skill)
        buffer.writeDouble(jump)
        buffer.writeDouble(stamina)
    }

    companion object {
        val ID = cobblemonResource("c2s_update_ride_stats")
        fun decode(buffer: RegistryFriendlyByteBuf): ServerboundUpdateRidingStatsPacket {
            return ServerboundUpdateRidingStatsPacket(
                entity = buffer.readInt(),
                ridingStyle = buffer.readEnum(RidingStyle::class.java),
                speed = buffer.readDouble(),
                acceleration = buffer.readDouble(),
                skill = buffer.readDouble(),
                jump = buffer.readDouble(),
                stamina = buffer.readDouble()
            )
        }
    }
}
