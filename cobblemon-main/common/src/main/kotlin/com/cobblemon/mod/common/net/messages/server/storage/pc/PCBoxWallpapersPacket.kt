/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.storage.pc

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.UnsplittablePacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

open class PCBoxWallpapersPacket internal constructor(val wallpapers: Set<ResourceLocation>) : NetworkPacket<PCBoxWallpapersPacket>, UnsplittablePacket {
    override val id = ID

    companion object {
        val ID = cobblemonResource("pc_box_wallpapers")
        fun decode(buffer: RegistryFriendlyByteBuf): PCBoxWallpapersPacket =
            PCBoxWallpapersPacket(buffer.readList { reader -> reader.readResourceLocation() }.toSet())
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(wallpapers) { writer, value -> writer.writeResourceLocation(value) }
    }
}
