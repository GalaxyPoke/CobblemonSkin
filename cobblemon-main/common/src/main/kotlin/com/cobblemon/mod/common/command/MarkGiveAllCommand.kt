/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.mark.Marks
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.player
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

object MarkGiveAllCommand {

    private const val NAME = "giveallmarks"
    private const val PLAYER = "player"
    private const val SLOT = "slot"

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val command = Commands.literal(NAME)
            .permission(CobblemonPermissions.CHANGE_MARK)
            .then(Commands.argument(PLAYER, EntityArgument.player())
                .then(Commands.argument(SLOT, PartySlotArgumentType.partySlot())
                    .executes { execute(it, it.player()) }
            ))
        dispatcher.register(command)
    }

    private fun execute(context: CommandContext<CommandSourceStack>, player: ServerPlayer) : Int {
        val pokemon = PartySlotArgumentType.getPokemonOf(context, SLOT, player)

        pokemon.marks = Marks.all().toMutableSet()

        val givenMessage = commandLang(NAME, player.name, pokemon.getDisplayName())
        context.source.sendSuccess({ givenMessage }, true)

        if (context.source.player?.equals(player) != true) {
            player.sendSystemMessage(givenMessage)
        }

        return Command.SINGLE_SUCCESS
    }
}
