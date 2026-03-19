/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.riding.behaviour.types.air.BirdSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.air.GliderSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.air.HelicopterSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.air.HoverSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.air.JetSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.air.RocketSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.land.HorseSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.land.MinekartSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.land.VehicleSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.liquid.BoatSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.liquid.BurstSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.liquid.DolphinSettings
import com.cobblemon.mod.common.api.riding.behaviour.types.liquid.SubmarineSettings
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Synchronizes the [com.cobblemon.mod.common.CobblemonRideSettings] registry.
 */
class RideSettingsSyncPacket(
    val bird: BirdSettings,
    val glider: GliderSettings,
    val helicopter: HelicopterSettings,
    val hover: HoverSettings,
    val jet: JetSettings,
    val rocket: RocketSettings,
    val horse: HorseSettings,
    val minekart: MinekartSettings,
    val vehicle: VehicleSettings,
    val boat: BoatSettings,
    val burst: BurstSettings,
    val dolphin: DolphinSettings,
    val submarine: SubmarineSettings
) : NetworkPacket<RideSettingsSyncPacket> {
    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        bird.encode(buffer)
        glider.encode(buffer)
        helicopter.encode(buffer)
        hover.encode(buffer)
        jet.encode(buffer)
        rocket.encode(buffer)
        horse.encode(buffer)
        minekart.encode(buffer)
        vehicle.encode(buffer)
        boat.encode(buffer)
        burst.encode(buffer)
        dolphin.encode(buffer)
        submarine.encode(buffer)
    }

    companion object {
        val ID = cobblemonResource("mechanics_sync")
        fun decode(buffer: RegistryFriendlyByteBuf): RideSettingsSyncPacket {
            return RideSettingsSyncPacket(
                bird = BirdSettings(),
                glider = GliderSettings(),
                helicopter = HelicopterSettings(),
                hover = HoverSettings(),
                jet = JetSettings(),
                rocket = RocketSettings(),
                horse = HorseSettings(),
                minekart = MinekartSettings(),
                vehicle = VehicleSettings(),
                boat = BoatSettings(),
                burst = BurstSettings(),
                dolphin = DolphinSettings(),
                submarine = SubmarineSettings()
            ).apply {
                bird.decode(buffer)
                glider.decode(buffer)
                helicopter.decode(buffer)
                hover.decode(buffer)
                jet.decode(buffer)
                rocket.decode(buffer)
                horse.decode(buffer)
                minekart.decode(buffer)
                vehicle.decode(buffer)
                boat.decode(buffer)
                burst.decode(buffer)
                dolphin.decode(buffer)
                submarine.decode(buffer)
            }
        }
    }
}