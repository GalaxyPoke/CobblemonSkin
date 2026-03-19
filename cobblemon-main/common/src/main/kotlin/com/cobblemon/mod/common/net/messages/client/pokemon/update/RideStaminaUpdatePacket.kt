/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Packet sent when the tracked stamina on Pokémon is updated. This is not the value that's
 * actively controlling riding but rather the tracking property which is put into the riding
 * state when the Pokémon is mounted.
 */
class RideStaminaUpdatePacket(pokemon: () -> Pokemon?, value: Float) : SingleUpdatePacket<Float, RideStaminaUpdatePacket>(pokemon, value) {
    companion object {
        val ID = com.cobblemon.mod.common.util.cobblemonResource("ride_stamina_update")
        fun decode(buffer: RegistryFriendlyByteBuf): RideStaminaUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val value = buffer.readFloat()
            return RideStaminaUpdatePacket(pokemon, value)
        }
    }

    override fun set(pokemon: Pokemon, value: Float) {
        Cobblemon.LOGGER.info("Updating client with $value")
        pokemon.rideStamina = value
    }

    override val id = ID
    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeFloat(this.value)
    }
}