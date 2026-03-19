/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.behaviour.BehaviourEditorScreen
import com.cobblemon.mod.common.entity.MoLangScriptingEntity
import com.cobblemon.mod.common.net.messages.client.OpenBehaviourEditorPacket
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.LivingEntity

object OpenBehaviourEditorHandler : ClientNetworkPacketHandler<OpenBehaviourEditorPacket> {
    override fun handle(packet: OpenBehaviourEditorPacket, client: Minecraft) {
        val entity = client.level?.getEntity(packet.entityId) as? MoLangScriptingEntity
        if (entity != null && entity is LivingEntity) {
            client.setScreen(BehaviourEditorScreen(entity, packet.appliedPresets.toMutableSet()))
        }
    }
}