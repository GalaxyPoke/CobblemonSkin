/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.debug

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class OpenRidingStatsDebugGUIPacket : NetworkPacket<OpenRidingStatsDebugGUIPacket> {
    override val id = ID

    companion object {
        val ID = cobblemonResource("open_riding_stats_debug_gui")

        fun decode(buffer: RegistryFriendlyByteBuf): OpenRidingStatsDebugGUIPacket {
            return OpenRidingStatsDebugGUIPacket()
        }
    }

    override fun encode(buf: RegistryFriendlyByteBuf) {}
}