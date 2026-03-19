/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.riding

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.duck.RidePassenger
import com.cobblemon.mod.common.net.messages.server.riding.ServerboundUpdateRiderRotationPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object ServerboundUpdateRiderRotationHandler : ServerNetworkPacketHandler<ServerboundUpdateRiderRotationPacket> {

    override fun handle(
        packet: ServerboundUpdateRiderRotationPacket,
        server: MinecraftServer,
        player: ServerPlayer
    ) {
        if (player is RidePassenger) {
            player.`cobblemon$setRideXRot`(packet.riderXRot)
            player.`cobblemon$setRideYRot`(packet.riderYRot)
            player.`cobblemon$setRideEyePos`(packet.rideEyePos)
        }
    }
}