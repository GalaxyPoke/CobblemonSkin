/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.debug

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.api.permission.CobblemonPermissions.USE_RIDING_STATS_DEBUG
import com.cobblemon.mod.common.api.riding.RidingStyle
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.debug.ServerboundUpdateRidingSettingsPacket
import com.cobblemon.mod.common.util.asExpression
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object ServerboundUpdateRidingSettingsHandler : ServerNetworkPacketHandler<ServerboundUpdateRidingSettingsPacket> {

    override fun handle(packet: ServerboundUpdateRidingSettingsPacket, server: MinecraftServer, player: ServerPlayer) {
        if (!Cobblemon.config.enableDebugKeys) return
        if (!Cobblemon.permissionValidator.hasPermission(player, USE_RIDING_STATS_DEBUG)) return

        val entity = player.level().getEntity(packet.entity) ?: return
        if (entity !is PokemonEntity) return
        if (entity.controllingPassenger != player) return
        this.modifyRideSettingsExpression(entity, packet.ridingStyle, packet.variable, packet.expression)
    }

    internal fun modifyRideSettingsExpression(vehicle: PokemonEntity, ridingStyle: RidingStyle, variable: String, expression: String) {
        val rideSettings = vehicle.ridingController?.behaviours?.get(ridingStyle) ?: return
        val clazz = rideSettings.javaClass
        val field = clazz.declaredFields.firstOrNull { it.name == variable } ?: return
        field.isAccessible = true
        field.set(rideSettings, expression.asExpression())
    }

}
