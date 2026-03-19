/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pokemon.update

import com.cobblemon.mod.common.api.riding.stats.RidingStat
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readEnumConstant
import com.cobblemon.mod.common.util.writeEnumConstant
import net.minecraft.network.RegistryFriendlyByteBuf

class RideBoostsUpdatePacket(pokemon: () -> Pokemon?, rideBoosts: Map<RidingStat, Float>) : SingleUpdatePacket<Map<RidingStat, Float>, RideBoostsUpdatePacket>(pokemon, rideBoosts) {
    companion object {
        val ID = cobblemonResource("ride_boosts_update")
        fun decode(buffer: RegistryFriendlyByteBuf): RideBoostsUpdatePacket {
            val pokemon = decodePokemon(buffer)
            val rideBoosts = buffer.readMap({ buffer.readEnumConstant(RidingStat::class.java) }, { buffer.readFloat() })
            return RideBoostsUpdatePacket(pokemon, rideBoosts)
        }
    }

    override val id = ID
    override fun encodeValue(buffer: RegistryFriendlyByteBuf) {
        buffer.writeMap(
            value,
            { _, it -> buffer.writeEnumConstant(it) },
            { _, it -> buffer.writeFloat(it) }
        )
    }

    override fun set(pokemon: Pokemon, value: Map<RidingStat, Float>) {
        pokemon.setRideBoosts(value)
    }
}
