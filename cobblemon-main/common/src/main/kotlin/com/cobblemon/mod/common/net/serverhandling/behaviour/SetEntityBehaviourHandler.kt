/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.serverhandling.behaviour

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.entity.BehaviourEditingTracker
import com.cobblemon.mod.common.entity.MoLangScriptingEntity
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.net.messages.client.npc.OpenNPCEditorPacket
import com.cobblemon.mod.common.net.messages.server.behaviour.SetEntityBehaviourPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity

object SetEntityBehaviourHandler : ServerNetworkPacketHandler<SetEntityBehaviourPacket> {
    override fun handle(packet: SetEntityBehaviourPacket, server: MinecraftServer, player: ServerPlayer) {
        val entity = player.serverLevel().getEntity(packet.entityId)
        if (entity == null || entity !is MoLangScriptingEntity || entity !is LivingEntity) {
            return player.closeContainer()
        }

        if (!BehaviourEditingTracker.isPlayerEditing(player, entity)) {
            return // Someone hacking maybe, or someone else got in and started editing while they were in here.
        }

        entity.updateBehaviours(packet.behaviours)

        if (entity is NPCEntity) {
            player.sendPacket(OpenNPCEditorPacket(entity))
        } else {
            BehaviourEditingTracker.stopEditing(player.uuid)
            player.closeContainer()
        }
    }
}