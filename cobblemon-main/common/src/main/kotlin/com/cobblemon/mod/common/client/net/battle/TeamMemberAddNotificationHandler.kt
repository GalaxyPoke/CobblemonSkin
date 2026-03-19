/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.battle

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.api.text.aqua
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.client.ClientMultiBattleTeamMember
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.render.ClientPlayerIcon
import com.cobblemon.mod.common.net.messages.client.battle.TeamMemberAddNotificationPacket
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft

object TeamMemberAddNotificationHandler : ClientNetworkPacketHandler<TeamMemberAddNotificationPacket> {
    override fun handle(packet: TeamMemberAddNotificationPacket, client: Minecraft) {

        if (CobblemonClient.teamData.multiBattleTeamMembers.any { it.uuid == packet.teamMemberUUID }) {
            return //already knows about the new member
        }
        var newMember = ClientMultiBattleTeamMember(packet.teamMemberUUID, packet.teamMemberName)
        CobblemonClient.teamData.multiBattleTeamMembers.add(newMember)
        ClientPlayerIcon.update(newMember.uuid)

        client.player?.sendSystemMessage(
            lang(
                "team.join.other",
                newMember.name.copy().aqua(),
            ).green()
        )
    }
}