/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage.party

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes
import com.cobblemon.mod.common.net.messages.server.storage.party.PartyTutorialPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object PartyTutorialPacketHandler : ServerNetworkPacketHandler<PartyTutorialPacket> {
    override fun handle(packet: PartyTutorialPacket, server: MinecraftServer, player: ServerPlayer) {
        val pd = Cobblemon.playerDataManager.getGenericData(player)

        if (!pd.partySelectTutorialDone) {
            pd.partySelectTutorialDone = true
            Cobblemon.playerDataManager.saveSingle(pd, PlayerInstancedDataStoreTypes.GENERAL)
            pd.sendToPlayer(player)
        }
    }
}