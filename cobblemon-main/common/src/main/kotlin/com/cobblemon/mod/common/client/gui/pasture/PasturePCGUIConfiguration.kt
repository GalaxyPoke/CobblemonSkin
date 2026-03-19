/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pasture

import com.cobblemon.mod.common.CobblemonNetwork
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.pasture.PasturePermissions
import com.cobblemon.mod.common.api.reactive.SettableObservable
import com.cobblemon.mod.common.client.gui.pc.PCGUIConfiguration
import com.cobblemon.mod.common.net.messages.client.pasture.OpenPasturePacket
import com.cobblemon.mod.common.net.messages.server.pasture.PasturePokemonPacket
import java.util.*

class PasturePCGUIConfiguration(
    val pastureId: UUID,
    val limit: Int,
    val pasturedPokemon: SettableObservable<List<OpenPasturePacket.PasturePokemonDataDTO>>,
    var permissions: PasturePermissions
) : PCGUIConfiguration(
    exitFunction = { it.closeNormally(unlink = true) },
    showParty = false,
    selectOverride = { pcGui, position, pokemon ->
        if (pokemon != null && !pokemon.isFainted() && pokemon.tetheringId == null && permissions.canPasture) {
            CobblemonNetwork.sendToServer(PasturePokemonPacket(pokemonId = pokemon.uuid, pastureId = pastureId))
            pcGui.playSound(CobblemonSounds.PC_CLICK)
        }
    },
    canSelect = { pokemon -> !pokemon.isFainted() && pokemon.tetheringId == null }
)