/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.storage

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.SendOutPokemonPacket
import com.cobblemon.mod.common.pokemon.activestate.ActivePokemonState
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState
import com.cobblemon.mod.common.util.raycastSafeSendout
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ClipContext

object SendOutPokemonHandler : ServerNetworkPacketHandler<SendOutPokemonPacket> {

    const val THROW_DURATION = 0.5F
    const val SEND_OUT_DURATION = 1.5F
    const val SEND_OUT_STAGGER_BASE_DURATION = 0.35F
    const val SEND_OUT_STAGGER_RANDOM_MAX_DURATION = 0.15F

    override fun handle(packet: SendOutPokemonPacket, server: MinecraftServer, player: ServerPlayer) {
        val slot = packet.slot.takeIf { it >= 0 } ?: return
        val party = Cobblemon.storage.getParty(player)
        val pokemon = party.get(slot) ?: return
        if (pokemon.isFainted()) {
            return
        }
        val state = pokemon.state
        if (state is ShoulderedState || state !is ActivePokemonState) {
            val position = player.raycastSafeSendout(pokemon, 12.0, 5.0, ClipContext.Fluid.ANY)

            if (position != null) {
                pokemon.sendOutWithAnimation(player, player.serverLevel(), position)
            }
        } else {
            val entity = state.entity
            when {
                entity == null -> pokemon.recall()
                else -> recallWithAnimation(entity)
            }
        }
    }

    private fun recallWithAnimation(pokemon: PokemonEntity) {
        // Calculate time until ball throw animation would be finished nicely
        val buffer = THROW_DURATION + 0.5F - pokemon.ticksLived.toFloat() / 20F
        if (buffer > 0.75F) {
            // Do nothing if the recall button is spammed too fast
        } else if (buffer > 0) {
            // If the recall button is pressed early,
            // buffer the recall instruction until after ball throw animation is complete
            pokemon.after(buffer) { pokemon.recallWithAnimation() }
        } else {
            // Recall normally
            pokemon.recallWithAnimation()
        }
    }
}
