/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.behaviour

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class SetEntityBehaviourPacket(
    val entityId: Int,
    val behaviours: Set<ResourceLocation>
): NetworkPacket<SetEntityBehaviourPacket> {
    companion object {
        val ID = cobblemonResource("set_entity_behaviour")
        fun decode(buffer: RegistryFriendlyByteBuf): SetEntityBehaviourPacket = SetEntityBehaviourPacket(
            entityId = buffer.readInt(),
            behaviours = buffer.readList { buffer.readIdentifier() }.toSet()
        )
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeInt(entityId)
        buffer.writeCollection(behaviours) { _, it -> buffer.writeIdentifier(it) }
    }
}