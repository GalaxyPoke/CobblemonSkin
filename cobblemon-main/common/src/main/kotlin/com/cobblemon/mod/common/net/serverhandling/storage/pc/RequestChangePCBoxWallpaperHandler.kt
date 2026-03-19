/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage.pc

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonUnlockableWallpapers
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.storage.ChangePCBoxWallpaperEvent
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.wallpaper.ChangePCBoxWallpaperPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.RequestChangePCBoxWallpaperPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object RequestChangePCBoxWallpaperHandler : ServerNetworkPacketHandler<RequestChangePCBoxWallpaperPacket> {
    override fun handle(packet: RequestChangePCBoxWallpaperPacket, server: MinecraftServer, player: ServerPlayer) {
        val pc = PCLinkManager.getPC(player) ?: return run { ClosePCPacket(null).sendToPlayer(player) }
        if (pc.boxes.size <= packet.boxNumber || Cobblemon.wallpapers[player.uuid]?.contains(packet.wallpaper) == false) {
            return
        }

        CobblemonUnlockableWallpapers.unlockableWallpapers.values.find { it.texture == packet.wallpaper }?.let { unlockable ->
            if (!unlockable.enabled || unlockable.id !in pc.unlockedWallpapers) {
                // Bro did you just try to hack on a wallpaper? How embarrassing.
                return
            }
        }

        val box = pc.boxes[packet.boxNumber]
        val event = ChangePCBoxWallpaperEvent.Pre(player, box, packet.wallpaper, packet.altWallpaper)
        CobblemonEvents.CHANGE_PC_BOX_WALLPAPER_EVENT_PRE.postThenFinally(
            event = event,
            ifSucceeded = { preEvent ->
                box.wallpaper = preEvent.altWallpaper?.let { preEvent.altWallpaper } ?: preEvent.wallpaper
                CobblemonEvents.CHANGE_PC_BOX_WALLPAPER_EVENT_POST.post(ChangePCBoxWallpaperEvent.Post(player, box, preEvent.wallpaper, preEvent.altWallpaper))
            },
            finally = {
                ChangePCBoxWallpaperPacket(pc.uuid, packet.boxNumber, box.wallpaper).sendToPlayer(player)
            }
        )
    }
}
