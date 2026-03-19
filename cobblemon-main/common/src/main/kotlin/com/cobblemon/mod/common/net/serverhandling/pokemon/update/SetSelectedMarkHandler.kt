/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.pokemon.update

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.storage.PokemonStore
import com.cobblemon.mod.common.net.messages.server.pokemon.update.SetActiveMarkPacket
import com.cobblemon.mod.common.util.party
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object SetActiveMarkHandler : ServerNetworkPacketHandler<SetActiveMarkPacket> {

    override fun handle(packet: SetActiveMarkPacket, server: MinecraftServer, player: ServerPlayer) {

        val pokemonStore: PokemonStore<*> = player.party()
        val pokemon = pokemonStore[packet.uuid] ?: return

        pokemon.activeMark = packet.mark
    }
}
