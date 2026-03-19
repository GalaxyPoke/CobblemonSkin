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

class ServerboundUpdateRidingStatRangePacket(
    val entity: Int,
    val ridingStyle: RidingStyle,
    val minSpeed: Int,
    val maxSpeed: Int,
    val minAcceleration: Int,
    val maxAcceleration: Int,
    val minSkill: Int,
    val maxSkill: Int,
    val minJump: Int,
    val maxJump: Int,
    val minStamina: Int,
    val maxStamina: Int
) : NetworkPacket<ServerboundUpdateRidingStatRangePacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(entity)
        buffer.writeEnum(ridingStyle)
        buffer.writeInt(minSpeed)
        buffer.writeInt(maxSpeed)
        buffer.writeInt(minAcceleration)
        buffer.writeInt(maxAcceleration)
        buffer.writeInt(minSkill)
        buffer.writeInt(maxSkill)
        buffer.writeInt(minJump)
        buffer.writeInt(maxJump)
        buffer.writeInt(minStamina)
        buffer.writeInt(maxStamina)
    }

    companion object {
        val ID = cobblemonResource("c2s_update_ride_stat_range")
        fun decode(buffer: RegistryFriendlyByteBuf): ServerboundUpdateRidingStatRangePacket {
            return ServerboundUpdateRidingStatRangePacket(
                entity = buffer.readInt(),
                ridingStyle = buffer.readEnum(RidingStyle::class.java),
                minSpeed = buffer.readInt(),
                maxSpeed = buffer.readInt(),
                minAcceleration = buffer.readInt(),
                maxAcceleration = buffer.readInt(),
                minSkill = buffer.readInt(),
                maxSkill = buffer.readInt(),
                minJump = buffer.readInt(),
                maxJump = buffer.readInt(),
                minStamina = buffer.readInt(),
                maxStamina = buffer.readInt()
            )
        }
    }
}
