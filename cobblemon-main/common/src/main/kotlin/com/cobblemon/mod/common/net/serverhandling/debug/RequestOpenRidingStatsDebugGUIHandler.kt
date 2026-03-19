/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.debug

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.permission.CobblemonPermissions.USE_RIDING_STATS_DEBUG
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.client.debug.RequestOpenRidingStatsDebugGUIPacket
import com.cobblemon.mod.common.net.messages.server.debug.OpenRidingStatsDebugGUIPacket
import com.cobblemon.mod.common.util.lang
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object RequestOpenRidingStatsDebugGUIHandler : ServerNetworkPacketHandler<RequestOpenRidingStatsDebugGUIPacket> {
    override fun handle(
        packet: RequestOpenRidingStatsDebugGUIPacket,
        server: MinecraftServer,
        player: ServerPlayer
    ) {
        if (!Cobblemon.config.enableDebugKeys) {
            player.sendSystemMessage(lang("requires_debug_keys").red())
            return
        }

        if (!Cobblemon.permissionValidator.hasPermission(player, USE_RIDING_STATS_DEBUG)) {
            player.sendSystemMessage(lang("requires_permission").red())
            return
        }

        OpenRidingStatsDebugGUIPacket().sendToPlayer(player)
    }
}