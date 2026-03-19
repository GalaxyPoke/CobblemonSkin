/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage.pc.wallpaper

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.UnsplittablePacket
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import java.util.*

open class ChangePCBoxWallpaperPacket internal constructor(val storeID: UUID, val boxNumber: Int, val wallpaper: ResourceLocation) : NetworkPacket<ChangePCBoxWallpaperPacket>, UnsplittablePacket {

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(storeID)
        buffer.writeSizedInt(IntSize.U_SHORT, boxNumber)
        buffer.writeString(wallpaper.toString())
    }

    companion object {
        val ID = cobblemonResource("change_pc_box_wallpaper")
        fun decode(buffer: RegistryFriendlyByteBuf): ChangePCBoxWallpaperPacket {
            val storeID = buffer.readUUID()
            val boxNumber = buffer.readSizedInt(IntSize.U_SHORT)
            val wallpaper = ResourceLocation.parse(buffer.readString())
            return ChangePCBoxWallpaperPacket(storeID, boxNumber, wallpaper)
        }
    }
}
