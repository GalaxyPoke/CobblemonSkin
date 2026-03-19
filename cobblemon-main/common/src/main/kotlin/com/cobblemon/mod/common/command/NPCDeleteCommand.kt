/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.green
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.world.entity.Entity

object NPCDeleteCommand {
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("npcdelete")
                .permission(CobblemonPermissions.NPC_DELETE)
                .then(
                    Commands.argument("target", EntityArgument.entity())
                        .executes {context ->
                            val source = context.source
                            val entity = EntityArgument.getEntity(context, "target")
                            execute(entity, source)
                        }
                )
                .executes {context ->
                    val source = context.source
                    val player = source.player
                    if (player == null) {
                        source.sendSystemMessage(commandLang("npcdelete.not_player").red())
                        return@executes 0
                    }

                    val targetEntity = player.traceFirstEntityCollision(entityClass = NPCEntity::class.java)

                    if (targetEntity == null) {
                        player.sendSystemMessage(commandLang("npcedit.non_npc").red())
                        return@executes 0
                    }
                    execute(targetEntity, source)
                }

        )
    }

    private fun execute(entity: Entity, source: CommandSourceStack) : Int {
        if(entity !is NPCEntity){
            source.sendSystemMessage(commandLang("npcedit.non_npc").red())
            return 0
        }

        entity.discard()
        source.sendSystemMessage(commandLang("npcdelete.deleted", entity.name.string).green())
        return Command.SINGLE_SUCCESS
    }
}