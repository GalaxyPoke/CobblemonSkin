/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.storage.pc

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.render.gui.PCBoxWallpaperRepository
import com.cobblemon.mod.common.net.messages.client.storage.pc.wallpaper.SetPCBoxWallpapersPacket
import net.minecraft.client.Minecraft

object SetPCBoxWallpapersHandler : ClientNetworkPacketHandler<SetPCBoxWallpapersPacket> {
    override fun handle(packet: SetPCBoxWallpapersPacket, client: Minecraft) {
        PCBoxWallpaperRepository.availableWallpapers = packet.wallpapers.toMutableSet()
    }
}