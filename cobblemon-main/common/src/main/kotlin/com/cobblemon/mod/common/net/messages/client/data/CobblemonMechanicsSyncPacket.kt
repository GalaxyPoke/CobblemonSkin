/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.mechanics.AprijuicesMechanic
import com.cobblemon.mod.common.mechanics.BerriesMechanic
import com.cobblemon.mod.common.mechanics.PotionsMechanic
import com.cobblemon.mod.common.mechanics.RemediesMechanic
import com.cobblemon.mod.common.mechanics.SlowpokeTailsMechanic
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class CobblemonMechanicsSyncPacket(
    val remedies: RemediesMechanic,
    val berries: BerriesMechanic,
    val potions: PotionsMechanic,
    val aprijuices: AprijuicesMechanic,
    val slowpokeTails: SlowpokeTailsMechanic
) : NetworkPacket<CobblemonMechanicsSyncPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        remedies.encode(buffer)
        berries.encode(buffer)
        potions.encode(buffer)
        aprijuices.encode(buffer)
        slowpokeTails.encode(buffer)
    }

    companion object {
        val ID = cobblemonResource("cobblemon_mechanics_sync")

        fun decode(buffer: RegistryFriendlyByteBuf): CobblemonMechanicsSyncPacket {
            return CobblemonMechanicsSyncPacket(
                RemediesMechanic.decode(buffer),
                BerriesMechanic.decode(buffer),
                PotionsMechanic.decode(buffer),
                AprijuicesMechanic.decode(buffer),
                SlowpokeTailsMechanic.decode(buffer)
            )
        }
    }
}