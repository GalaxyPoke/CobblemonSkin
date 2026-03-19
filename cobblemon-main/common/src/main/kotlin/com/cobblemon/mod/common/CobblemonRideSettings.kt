/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.data.DataRegistry
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
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
import com.cobblemon.mod.common.net.messages.client.data.RideSettingsSyncPacket
import com.cobblemon.mod.common.util.adapters.ExpressionAdapter
import com.cobblemon.mod.common.util.adapters.ExpressionLikeAdapter
import com.cobblemon.mod.common.util.adapters.FloatNumberRangeAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.GsonBuilder
import net.minecraft.advancements.critereon.MinMaxBounds
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager

object CobblemonRideSettings : DataRegistry {
    override val id: ResourceLocation = cobblemonResource("ride_settings")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<CobblemonRideSettings>()
    val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Expression::class.java, ExpressionAdapter)
        .registerTypeAdapter(ExpressionLike::class.java, ExpressionLikeAdapter)
        .registerTypeAdapter(MinMaxBounds.Doubles::class.java, FloatNumberRangeAdapter)
        .create()

    var bird = BirdSettings()
    var glider = GliderSettings()
    var helicopter = HelicopterSettings()
    var hover = HoverSettings()
    var jet = JetSettings()
    var rocket = RocketSettings()

    var horse = HorseSettings()
    var minekart = MinekartSettings()
    var vehicle = VehicleSettings()

    var boat = BoatSettings()
    var burst = BurstSettings()
    var dolphin = DolphinSettings()
    var submarine = SubmarineSettings()

    override fun sync(player: ServerPlayer) {
        player.sendPacket(
            RideSettingsSyncPacket(
                bird = bird,
                glider = glider,
                helicopter = helicopter,
                hover = hover,
                jet = jet,
                rocket = rocket,
                horse = horse,
                minekart = minekart,
                vehicle = vehicle,
                boat = boat,
                burst = burst,
                dolphin = dolphin,
                submarine = submarine
            )
        )
    }

    override fun reload(manager: ResourceManager) {
        bird = loadStyle(manager, "bird", BirdSettings::class.java)
        glider = loadStyle(manager, "glider", GliderSettings::class.java)
        helicopter = loadStyle(manager, "helicopter", HelicopterSettings::class.java)
        hover = loadStyle(manager, "hover", HoverSettings::class.java)
        jet = loadStyle(manager, "jet", JetSettings::class.java)
        rocket = loadStyle(manager, "rocket", RocketSettings::class.java)
        horse = loadStyle(manager, "horse", HorseSettings::class.java)
        minekart = loadStyle(manager, "minekart", MinekartSettings::class.java)
        vehicle = loadStyle(manager, "vehicle", VehicleSettings::class.java)
        boat = loadStyle(manager, "boat", BoatSettings::class.java)
        burst = loadStyle(manager, "burst", BurstSettings::class.java)
        dolphin = loadStyle(manager, "dolphin", DolphinSettings::class.java)
        submarine = loadStyle(manager, "submarine", SubmarineSettings::class.java)
    }

    private fun <T : RidingBehaviourSettings> loadStyle(manager: ResourceManager, name: String, clazz: Class<T>): T {
        manager.getResourceOrThrow(cobblemonResource("ride_settings/$name.json")).open().use {
            return gson.fromJson(it.reader(), clazz)
        }
    }
}