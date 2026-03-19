/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.storage.ChangePCBoxWallpaperEvent
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.net.messages.client.storage.pc.wallpaper.ChangePCBoxWallpaperPacket
import com.cobblemon.mod.common.util.commandLang
import com.cobblemon.mod.common.util.pc
import com.cobblemon.mod.common.util.permission
import com.cobblemon.mod.common.util.player
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

object ChangeBoxWallpaperCommand {
    private val BOX_DOES_NOT_EXIST = { boxNo: Int -> commandLang("pokebox.box_does_not_exist", boxNo) }
    private val WALLPAPER_DOES_NOT_EXIST = { wallpaper: String -> commandLang("changewallpaper.wallpaper_does_not_exist", wallpaper) }
    private val CANNOT_CHANGE_WALLPAPER = { name: String -> commandLang("changewallpaper.cannot_change_wallpaper", name) }

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(Commands.literal("changewallpaper")
            .permission(CobblemonPermissions.CHANGE_WALLPAPER)
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("box", IntegerArgumentType.integer(1))
                    .then(Commands.argument("wallpaper", StringArgumentType.greedyString())
                        .suggests(SuggestionProvider { context, builder ->
                            Cobblemon.wallpapers[EntityArgument.getPlayer(context, "player").uuid]?.forEach {
                                builder.suggest(it.toString())
                            }
                            builder.buildFuture()
                        })
                        .executes { context ->
                            val player = context.player()
                            val box = IntegerArgumentType.getInteger(context, "box")
                            val wallpaper = ResourceLocation.tryParse(StringArgumentType.getString(context, "wallpaper"))
                            execute(player, box, wallpaper)
                        }
                    )
                )
            ))
    }

    private fun execute(
        player: ServerPlayer,
        box: Int,
        wallpaper: ResourceLocation?
    ): Int {
        val playerPc = player.pc()
        if (playerPc.boxes.size < box) {
            throw SimpleCommandExceptionType(BOX_DOES_NOT_EXIST(box).red()).create()
        }

        if (wallpaper == null || Cobblemon.wallpapers[player.uuid]?.contains(wallpaper) == false) {
            throw SimpleCommandExceptionType(WALLPAPER_DOES_NOT_EXIST(wallpaper.toString()).red()).create()
        }

        val pcBox = playerPc.boxes[box - 1]
        CobblemonEvents.CHANGE_PC_BOX_WALLPAPER_EVENT_PRE.postThen(
            event = ChangePCBoxWallpaperEvent.Pre(player, pcBox, wallpaper, null),
            ifSucceeded = {
                pcBox.wallpaper = it.wallpaper
                CobblemonEvents.CHANGE_PC_BOX_WALLPAPER_EVENT_POST.post(ChangePCBoxWallpaperEvent.Post(player, pcBox, it.wallpaper, null))
                ChangePCBoxWallpaperPacket(playerPc.uuid, pcBox.boxNumber, it.wallpaper).sendToPlayer(player)
            },
            ifCanceled = {
                throw SimpleCommandExceptionType(CANNOT_CHANGE_WALLPAPER(wallpaper.toString()).red()).create()
            }
        )

        return Command.SINGLE_SUCCESS
    }
}