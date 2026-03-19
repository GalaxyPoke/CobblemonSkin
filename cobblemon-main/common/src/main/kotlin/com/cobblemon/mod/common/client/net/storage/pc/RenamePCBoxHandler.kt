/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.storage.pc

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.client.storage.pc.RenamePCBoxPacket
import net.minecraft.client.Minecraft

object RenamePCBoxHandler : ClientNetworkPacketHandler<RenamePCBoxPacket> {
    override fun handle(packet: RenamePCBoxPacket, client: Minecraft) {
        CobblemonClient.storage.renameBox(packet.storeID, packet.boxNumber, packet.name)
    }
}