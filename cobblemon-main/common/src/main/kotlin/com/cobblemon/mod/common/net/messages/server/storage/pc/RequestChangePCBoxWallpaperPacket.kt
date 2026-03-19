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
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeSizedInt
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import java.util.UUID

class RequestChangePCBoxWallpaperPacket internal constructor(val storeID: UUID, val boxNumber: Int, val wallpaper: ResourceLocation, val altWallpaper: ResourceLocation?) : NetworkPacket<RequestChangePCBoxWallpaperPacket>, UnsplittablePacket {

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(storeID)
        buffer.writeSizedInt(IntSize.U_SHORT, boxNumber)
        buffer.writeString(wallpaper.toString())
        buffer.writeNullable(altWallpaper) { _, value -> buffer.writeString(value.toString()) }
    }

    companion object {
        val ID = cobblemonResource("request_change_pc_box_wallpaper")
        fun decode(buffer: RegistryFriendlyByteBuf): RequestChangePCBoxWallpaperPacket {
            val storeID = buffer.readUUID()
            val boxNumber = buffer.readSizedInt(IntSize.U_SHORT)
            val wallpaper = ResourceLocation.parse(buffer.readString())
            val altWallpaper = (buffer.readNullable { buffer.readString() })?.let { ResourceLocation.parse(it) }
            return RequestChangePCBoxWallpaperPacket(storeID, boxNumber, wallpaper, altWallpaper)
        }
    }
}
