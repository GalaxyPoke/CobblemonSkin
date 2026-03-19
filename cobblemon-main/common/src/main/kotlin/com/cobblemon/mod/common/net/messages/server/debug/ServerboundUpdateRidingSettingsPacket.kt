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
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

class ServerboundUpdateRidingSettingsPacket(
    val entity: Int,
    val ridingStyle: RidingStyle,
    val variable: String,
    val expression: String
) : NetworkPacket<ServerboundUpdateRidingSettingsPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(entity)
        buffer.writeEnum(ridingStyle)
        buffer.writeString(variable)
        buffer.writeString(expression)
    }

    companion object {
        val ID = cobblemonResource("c2s_update_ride_settings")
        fun decode(buffer: RegistryFriendlyByteBuf): ServerboundUpdateRidingSettingsPacket {
            return ServerboundUpdateRidingSettingsPacket(
                entity = buffer.readInt(),
                ridingStyle = buffer.readEnum(RidingStyle::class.java),
                variable = buffer.readString(),
                expression = buffer.readString()
            )
        }
    }
}
