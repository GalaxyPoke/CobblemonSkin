/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.pasture

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.pc.PCGUI
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag
import com.cobblemon.mod.common.net.messages.client.pasture.UpdatePastureConflictFlagPacket
import net.minecraft.client.Minecraft

object UpdatePastureConflictFlagHandler : ClientNetworkPacketHandler<UpdatePastureConflictFlagPacket> {
    override fun handle(packet: UpdatePastureConflictFlagPacket, client: Minecraft) {
        val screen = client.screen as? PCGUI ?: return
        val pastureWidget = screen.storage.pastureWidget ?: return
        for (slot in pastureWidget.pastureScrollList.children()) {
            if (slot.pokemon.pokemonId == packet.pokemonId) {
                // Update behavior flags
                slot.pokemon.behaviourFlags = if (packet.enabled) {
                    slot.pokemon.behaviourFlags + PokemonBehaviourFlag.PASTURE_CONFLICT
                } else {
                    slot.pokemon.behaviourFlags - PokemonBehaviourFlag.PASTURE_CONFLICT
                }

                slot.conflictButton.setEnabled(packet.enabled)
                break
            }
        }
    }
}