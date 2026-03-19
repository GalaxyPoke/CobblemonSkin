/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.storage.pc

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * Packet sent to the server to indicate that some set of wallpapers have now been seen and don't need to be presented
 * as new the next time the PC is opened. The packet is sent once the wallpapers button is clicked.
 *
 * @author Hiroku
 * @since February 10th, 2025
 */
class MarkPCBoxWallpapersSeenPacket(val seenTextures: Set<ResourceLocation>) : NetworkPacket<MarkPCBoxWallpapersSeenPacket> {
    companion object {
        val ID = cobblemonResource("mark_pc_box_wallpapers_seen")
        fun decode(buffer: RegistryFriendlyByteBuf) = MarkPCBoxWallpapersSeenPacket(buffer.readList { ResourceLocation.parse(it.readString()) }.toSet())
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(seenTextures.map { it.toString() }) { _, it -> buffer.writeString(it) }
    }
}