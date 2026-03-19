/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage.pc.wallpaper

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * Packet sent to the client when a new PC box wallpaper is unlocked. This adds the texture to the
 * client-side list of known and usable wallpapers.
 *
 * @author Hiroku
 * @since February 10th, 2025
 */
class UnlockPCBoxWallpaperPacket(val texture: ResourceLocation) : NetworkPacket<UnlockPCBoxWallpaperPacket> {
    companion object {
        val ID = cobblemonResource("unlock_pc_box_wallpaper")
        fun decode(buffer: RegistryFriendlyByteBuf) = UnlockPCBoxWallpaperPacket(ResourceLocation.parse(buffer.readString()))
    }

    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(texture.toString())
    }
}