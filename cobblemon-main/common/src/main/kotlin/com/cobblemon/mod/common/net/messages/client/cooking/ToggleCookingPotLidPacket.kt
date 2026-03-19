/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.cooking

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class ToggleCookingPotLidPacket(val value: Boolean) : NetworkPacket<ToggleCookingPotLidPacket> {
    companion object {
        val ID = cobblemonResource("toggle_cooking_pot_lid")
        fun decode(buffer: RegistryFriendlyByteBuf): ToggleCookingPotLidPacket {
            return ToggleCookingPotLidPacket(buffer.readBoolean())
        }
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBoolean(this.value)
    }
}