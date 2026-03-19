/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.battles.ChallengeManager
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.BattleChallengeResponsePacket
import com.cobblemon.mod.common.util.party
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

/**
 * Processes a player's response to a [BattleChallenge].
 *
 * @author JazzMcNade
 * @since March 12th, 2024
 */
object ChallengeResponseHandler : ServerNetworkPacketHandler<BattleChallengeResponsePacket> {
    override fun handle(packet: BattleChallengeResponsePacket, server: MinecraftServer, player: ServerPlayer) {
        val targetedEntity = player.level().getEntity(packet.targetedEntityId)?.let {
            when (it) {
                is PokemonEntity -> it.owner
                is ServerPlayer -> it
                else -> null
            }
        } ?: return

        if (targetedEntity !is ServerPlayer) return

        val leadingPokemon = player.party()[packet.selectedPokemonId]?.uuid ?: return   // server-side validation
        ChallengeManager.setLead(player, leadingPokemon)
        if (packet.accept)
            ChallengeManager.acceptRequest(player, packet.requestID, targetedEntity)
        else
            ChallengeManager.declineRequest(player, packet.requestID)

    }
}