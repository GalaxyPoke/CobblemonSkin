/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class OpenBehaviourEditorPacket(val entityId: Int, val appliedPresets: Set<ResourceLocation>) : NetworkPacket<OpenBehaviourEditorPacket> {
    companion object {
        val ID = cobblemonResource("open_behaviour_editor")
        fun decode(buffer: RegistryFriendlyByteBuf) = OpenBehaviourEditorPacket(
            buffer.readInt(),
            buffer.readList { it.readIdentifier() }.toSet()
        )
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(entityId)
        buffer.writeCollection(appliedPresets) { _, it -> buffer.writeIdentifier(it) }
    }
}