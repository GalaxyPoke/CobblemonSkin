/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage.pc

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.storage.RenamePCBoxEvent
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.RenamePCBoxPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.RequestRenamePCBoxPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object RequestRenamePCBoxHandler : ServerNetworkPacketHandler<RequestRenamePCBoxPacket> {
    override fun handle(packet: RequestRenamePCBoxPacket, server: MinecraftServer, player: ServerPlayer) {
        val pc = PCLinkManager.getPC(player) ?: return run { ClosePCPacket(null).sendToPlayer(player) }
        if (pc.boxes.size <= packet.boxNumber) {
            return
        }

        val box = pc.boxes[packet.boxNumber]
        if (packet.name != null && packet.name.length > 19) {
            RenamePCBoxPacket(pc.uuid, packet.boxNumber, box.name).sendToPlayer(player)
            return
        }

        CobblemonEvents.RENAME_PC_BOX_EVENT_PRE.postThenFinally(
            event = RenamePCBoxEvent.Pre(player, box, packet.name?: ""),
            ifSucceeded = { preEvent ->
                box.name = preEvent.name
                CobblemonEvents.RENAME_PC_BOX_EVENT_POST.post(RenamePCBoxEvent.Post(player, box, preEvent.name))
            },
            finally = {
                RenamePCBoxPacket(pc.uuid, packet.boxNumber, box.name).sendToPlayer(player)
            }
        )
    }
}