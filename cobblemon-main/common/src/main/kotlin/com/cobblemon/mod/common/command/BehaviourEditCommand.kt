/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.entity.BehaviourEditingTracker
import com.cobblemon.mod.common.entity.MoLangScriptingEntity
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.net.messages.client.OpenBehaviourEditorPacket
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.requiresWithPermission
import com.cobblemon.mod.common.util.traceFirstEntityCollision
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity

object BehaviourEditCommand {
    fun register(dispatcher : CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("behaviouredit")
                .requiresWithPermission(CobblemonPermissions.BEHAVIOUR_EDIT) { it.player != null }
                .executes { execute(it, it.source.playerOrException) })
    }

    private fun execute(context: CommandContext<CommandSourceStack>, player: ServerPlayer) : Int {
        val targetEntity = player.traceFirstEntityCollision(entityClass = LivingEntity::class.java, ignoreEntity = player)?.takeIf { it is MoLangScriptingEntity }
        if (targetEntity == null) {
            player.sendSystemMessage(commandLang("behaviouredit.non_scriptable").red())
            return 0
        }

        if (targetEntity is NPCEntity) {
            targetEntity.edit(player)
        } else {
            BehaviourEditingTracker.startEditing(player, targetEntity)
            player.sendPacket(OpenBehaviourEditorPacket(targetEntity.id, (targetEntity as MoLangScriptingEntity).behaviours.toSet()))
        }

        return Command.SINGLE_SUCCESS
    }
}