/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * Packet sent to the client to tell it to simulate animations on the client and generate the most logical
 * positions for the hitboxes.
 *
 * @author Hiroku
 * @since March 12th, 2025
 */
class CalculateSeatPositionsPacket(val speciesIdentifier: ResourceLocation, val aspects: Set<String>, val poseType: PoseType) : NetworkPacket<CalculateSeatPositionsPacket> {
    override val id: ResourceLocation = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeResourceLocation(speciesIdentifier)
        buffer.writeCollection(aspects) { _, aspect -> buffer.writeString(aspect) }
        buffer.writeString(poseType.name)
    }

    companion object {
        val ID = cobblemonResource("calculate_seat_positions")

        fun decode(buffer: RegistryFriendlyByteBuf): CalculateSeatPositionsPacket {
            return CalculateSeatPositionsPacket(
                buffer.readResourceLocation(),
                buffer.readList { _ -> buffer.readString() }.toSet(),
                PoseType.valueOf(buffer.readString().uppercase()),
            )
        }
    }
}