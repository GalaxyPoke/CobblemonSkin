/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.cooking

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.block.campfirepot.CookingPotMenu
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity
import com.cobblemon.mod.common.net.messages.client.cooking.ToggleCookingPotLidPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object ToggleCookingPotLidHandler : ServerNetworkPacketHandler<ToggleCookingPotLidPacket> {
    override fun handle(
        packet: ToggleCookingPotLidPacket,
        server: MinecraftServer,
        player: ServerPlayer
    ) {
        if (player.containerMenu !is CookingPotMenu) {
            Cobblemon.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
            return
        }

        val menu = player.containerMenu as? CookingPotMenu ?: return
        val isLidOpen = packet.value
        if (menu.container is CampfireBlockEntity) {
            menu.container.toggleLid(isLidOpen)
        }
    }
}