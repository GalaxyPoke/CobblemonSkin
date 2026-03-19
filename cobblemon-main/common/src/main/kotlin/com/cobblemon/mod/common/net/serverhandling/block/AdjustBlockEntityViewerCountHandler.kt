/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.block

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.block.entity.ViewerCountBlockEntity
import com.cobblemon.mod.common.net.messages.server.block.AdjustBlockEntityViewerCountPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object AdjustBlockEntityViewerCountHandler : ServerNetworkPacketHandler<AdjustBlockEntityViewerCountPacket> {
    override fun handle(packet: AdjustBlockEntityViewerCountPacket, server: MinecraftServer, player: ServerPlayer) {
        val blockEntity = player.level().getBlockEntity(packet.blockPos)
        if (blockEntity != null && blockEntity is ViewerCountBlockEntity) {
            if (packet.increment) blockEntity.incrementViewerCount()
            else blockEntity.decrementViewerCount()
        }
    }
}