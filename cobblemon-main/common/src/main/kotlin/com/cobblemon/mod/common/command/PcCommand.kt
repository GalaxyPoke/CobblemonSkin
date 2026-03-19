/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.storage.pc.link.PCLinkManager
import com.cobblemon.mod.common.api.storage.pc.link.PermissiblePcLink
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.client.storage.pc.OpenPCPacket
import com.cobblemon.mod.common.util.*
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.Commands.argument

object PcCommand {

    private const val NAME = "pc"
    private val IN_BATTLE_EXCEPTION = SimpleCommandExceptionType(lang("pc.inbattle").red())

    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(literal(NAME)
            .permission(CobblemonPermissions.PC)
            .then(argument("box", IntegerArgumentType.integer(1))
                .executes(this::execute)
            )
            .executes(this::execute)
        )
    }

    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val box = try {
            IntegerArgumentType.getInteger(context, "box")
        } catch (e: IllegalArgumentException) {
            null
        }
        val pc = player.pc()
        if (player.isInBattle()) {
            throw IN_BATTLE_EXCEPTION.create()
        }
        if (box != null && pc.boxes.size < box) {
            throw SimpleCommandExceptionType(lang("command.pc.invalid-box", box, pc.boxes.size).red()).create()
        }
        PCLinkManager.addLink(PermissiblePcLink(pc, player, CobblemonPermissions.PC))
        OpenPCPacket(pc, box = if (box != null) (box - 1) else null).sendToPlayer(player)
        context.source.level.playSoundServer(
            position = context.source.player!!.position(),
            sound = CobblemonSounds.PC_ON,
            volume = 0.5F,
            pitch = 1F
        )
        return Command.SINGLE_SUCCESS
    }

}