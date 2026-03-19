/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.data

import com.cobblemon.mod.common.CobblemonMechanics
import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.client.data.CobblemonMechanicsSyncPacket
import net.minecraft.client.Minecraft

object CobblemonMechanicsSyncHandler : ClientNetworkPacketHandler<CobblemonMechanicsSyncPacket> {

    override fun handle(packet: CobblemonMechanicsSyncPacket, client: Minecraft) {
        CobblemonMechanics.remedies = packet.remedies
        CobblemonMechanics.berries = packet.berries
        CobblemonMechanics.potions = packet.potions
        CobblemonMechanics.aprijuices = packet.aprijuices
        CobblemonMechanics.slowpokeTails = packet.slowpokeTails
    }
}