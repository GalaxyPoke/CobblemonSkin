/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.storage.RenamePCBoxEvent
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.client.storage.pc.RenamePCBoxPacket
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.pc
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.player
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

object RenameBoxCommand {
    private val BOX_DOES_NOT_EXIST = { boxNo: Int -> commandLang("pokebox.box_does_not_exist", boxNo) }
    private val CANNOT_RENAME_BOX = { name: String -> commandLang("renamebox.cannot_rename_box", name) }

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(Commands.literal("renamebox")
            .permission(CobblemonPermissions.RENAMEBOX)
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("box", IntegerArgumentType.integer(1))
                    .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes { context ->
                            val player = context.player()
                            val box = IntegerArgumentType.getInteger(context, "box")
                            val name = StringArgumentType.getString(context, "name")
                            execute(player, box, name)
                        }
                    )
                )
            ))
    }

    private fun execute(
        player: ServerPlayer,
        box: Int,
        name: String
    ): Int {
        val playerPc = player.pc()
        if (playerPc.boxes.size < box) {
            throw SimpleCommandExceptionType(BOX_DOES_NOT_EXIST(box).red()).create()
        }

        val pcBox = playerPc.boxes[box - 1]
        CobblemonEvents.RENAME_PC_BOX_EVENT_PRE.postThen(
            event = RenamePCBoxEvent.Pre(player, pcBox, name),
            ifSucceeded = {
                pcBox.name = it.name
                CobblemonEvents.RENAME_PC_BOX_EVENT_POST.post(RenamePCBoxEvent.Post(player, pcBox, it.name))
                RenamePCBoxPacket(playerPc.uuid, pcBox.boxNumber, it.name).sendToPlayer(player)
            },
            ifCanceled = {
                throw SimpleCommandExceptionType(CANNOT_RENAME_BOX(name).red()).create()
            }
        )

        return Command.SINGLE_SUCCESS
    }
}