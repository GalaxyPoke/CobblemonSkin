/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.net.messages.client.settings.OpenCobblemonConfigScreenPacket
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer

object CobblemonConfigCommand {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("cobblemonconfig")
                .executes {
                    it.source.playerOrException.sendPacket(OpenCobblemonConfigScreenPacket())
                    Command.SINGLE_SUCCESS
                }
                .then(
                    Commands.literal("reload")
                        .permission(CobblemonPermissions.COBBLEMON_CONFIG_RELOAD)
                        .executes {
                            Cobblemon.reloadConfig()
                            it.source.server.playerList.players.forEach { player ->
                                if (player is ServerPlayer) {
                                    Cobblemon.sendServerSettingsPacketToPlayer(player)
                                }
                            }

                            it.source.sendSuccess({ commandLang("cobblemon_config.reload") }, true)
                            Command.SINGLE_SUCCESS
                        }
                )
        )
    }
}
