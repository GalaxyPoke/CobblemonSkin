/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.debug

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.debug.riding.RidingStatsDebugGUI
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.debug.OpenRidingStatsDebugGUIPacket
import net.minecraft.client.Minecraft

object OpenRidingStatsDebugGUIHandler : ClientNetworkPacketHandler<OpenRidingStatsDebugGUIPacket> {
    override fun handle(
        packet: OpenRidingStatsDebugGUIPacket,
        client: Minecraft
    ) {
        val vehicle = Minecraft.getInstance().player?.vehicle as? PokemonEntity ?: return
        client.setScreen(RidingStatsDebugGUI(vehicle))
    }
}