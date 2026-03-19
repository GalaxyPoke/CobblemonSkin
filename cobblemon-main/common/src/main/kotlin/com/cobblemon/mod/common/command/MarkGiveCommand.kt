/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.command.argument.MarkArgumentType
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.player
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

object MarkGiveCommand {

    private const val NAME = "givemark"
    private const val PLAYER = "player"
    private const val SLOT = "slot"
    private const val MARK = "mark"
    private val ALREADY_HAS_EXCEPTION = Dynamic3CommandExceptionType { a, b, c -> commandLang("$NAME.already_has", a, b, c).red() }

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        val command = Commands.literal(NAME)
            .permission(CobblemonPermissions.CHANGE_MARK)
            .then(Commands.argument(PLAYER, EntityArgument.player())
                .then(Commands.argument(SLOT, PartySlotArgumentType.partySlot())
                    .then(Commands.argument(MARK, MarkArgumentType.mark())
                        .executes { execute(it, it.player()) }
                    ))
            )
        dispatcher.register(command)
    }

    private fun execute(context: CommandContext<CommandSourceStack>, player: ServerPlayer) : Int {
        val pokemon = PartySlotArgumentType.getPokemonOf(context, SLOT, player)
        val mark = MarkArgumentType.getMark(context, MARK)

        if (pokemon.marks.contains(mark)) {
            throw ALREADY_HAS_EXCEPTION.create(player.name, pokemon.getDisplayName(), mark.getName())
        }

        pokemon.exchangeMark(mark, true)

        val givenMessage = commandLang(NAME, player.name, pokemon.getDisplayName(), mark.getName())
        context.source.sendSuccess({ givenMessage }, true)

        if (context.source.player?.equals(player) != true) {
            player.sendSystemMessage(givenMessage)
        }

        return Command.SINGLE_SUCCESS
    }
}
