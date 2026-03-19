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
import com.cobblemon.mod.common.net.messages.server.pokemon.update.SetMarkingsPacket
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.pc
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object SetMarkingsHandler : ServerNetworkPacketHandler<SetMarkingsPacket> {

    override fun handle(packet: SetMarkingsPacket, server: MinecraftServer, player: ServerPlayer) {
        val pokemonStore: PokemonStore<*> = if (packet.isParty) player.party() else player.pc()
        val pokemon = pokemonStore[packet.uuid] ?: return

        pokemon.markings = packet.markings
    }
}
