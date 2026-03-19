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
import com.cobblemon.mod.common.api.events.storage.WallpaperCollectionEvent
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.storage.pc.PCBoxWallpapersPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.wallpaper.SetPCBoxWallpapersPacket
import com.cobblemon.mod.common.util.pc
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object PCBoxWallpapersHandler : ServerNetworkPacketHandler<PCBoxWallpapersPacket> {
    override fun handle(packet: PCBoxWallpapersPacket, server: MinecraftServer, player: ServerPlayer) {
        val pc = player.pc()

        /*
         * The client can have a resource pack for a lot of their own wallpaper textures, and the server can
         * setup some wallpaper textures that need to be unlocked. The logic here is a bit confusing but the idea
         * is that we find which server-defined wallpapers are marked as unlocked and the complement that are locked,
         *  and if the client has dictated that they have a wallpaper that is actually in the locked list, the server
         *  is probably trying to hide resource-packed wallpapers for some reason. Which is fine, I guess.
         */

        val (unlockedWallpapers, lockedWallpapers) = CobblemonUnlockableWallpapers.unlockableWallpapers.values
            .partition { it.enabled && it.id in pc.unlockedWallpapers }
            .let { it.first.map { it.texture } to it.second.map { it.texture } }

        // If it's considered an unlockable wallpaper by the server and it has not been unlocked yet, ignore the client's claim
        val regularWallpaperTextures = packet.wallpapers.filter { it !in lockedWallpapers }

        val wallpapers = mutableSetOf(*regularWallpaperTextures.toTypedArray(), *unlockedWallpapers.toTypedArray())
        CobblemonEvents.WALLPAPER_COLLECTION_EVENT.post(WallpaperCollectionEvent(player, wallpapers), then = { event ->
            Cobblemon.wallpapers[player.uuid] to event.wallpapers
            SetPCBoxWallpapersPacket(event.wallpapers).sendToPlayer(player)
        })
    }
}