package com.example.cobblemon_skin.command

import com.cobblemon.mod.common.Cobblemon
import com.example.cobblemon_skin.CobblemonSkinMod
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object SkinCommand {

    private val SKIN_SUGGESTIONS = SuggestionProvider<CommandSourceStack> { _, builder ->
        CobblemonSkinMod.registeredSkins.forEach { builder.suggest(it) }
        builder.buildFuture()
    }

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("pokemonskin")
                .requires { it.hasPermission(2) }

                // /pokemonskin set <skinId> [slot]
                .then(
                    Commands.literal("set")
                        .then(
                            Commands.argument("skinId", StringArgumentType.word())
                                .suggests(SKIN_SUGGESTIONS)
                                .executes { ctx ->
                                    val skinId = StringArgumentType.getString(ctx, "skinId")
                                    val player = ctx.source.playerOrException
                                    cmdSet(player, skinId, 1)
                                }
                                .then(
                                    Commands.argument("slot", IntegerArgumentType.integer(1, 6))
                                        .executes { ctx ->
                                            val skinId = StringArgumentType.getString(ctx, "skinId")
                                            val slot = IntegerArgumentType.getInteger(ctx, "slot")
                                            val player = ctx.source.playerOrException
                                            cmdSet(player, skinId, slot)
                                        }
                                )
                        )
                )

                // /pokemonskin clear [slot]
                .then(
                    Commands.literal("clear")
                        .executes { ctx ->
                            val player = ctx.source.playerOrException
                            cmdClear(player, 1)
                        }
                        .then(
                            Commands.argument("slot", IntegerArgumentType.integer(1, 6))
                                .executes { ctx ->
                                    val slot = IntegerArgumentType.getInteger(ctx, "slot")
                                    val player = ctx.source.playerOrException
                                    cmdClear(player, slot)
                                }
                        )
                )

                // /pokemonskin list
                .then(
                    Commands.literal("list")
                        .executes { ctx ->
                            cmdList(ctx.source)
                        }
                )

                // /pokemonskin info [slot]
                .then(
                    Commands.literal("info")
                        .executes { ctx ->
                            val player = ctx.source.playerOrException
                            cmdInfo(player, 1)
                        }
                        .then(
                            Commands.argument("slot", IntegerArgumentType.integer(1, 6))
                                .executes { ctx ->
                                    val slot = IntegerArgumentType.getInteger(ctx, "slot")
                                    val player = ctx.source.playerOrException
                                    cmdInfo(player, slot)
                                }
                        )
                )

                // /pokemonskin admin set <player> <skinId> [slot]
                // /pokemonskin admin clear <player> [slot]
                .then(
                    Commands.literal("admin")
                        .requires { it.hasPermission(3) }
                        .then(
                            Commands.literal("set")
                                .then(
                                    Commands.argument("target", EntityArgument.player())
                                        .then(
                                            Commands.argument("skinId", StringArgumentType.word())
                                                .suggests(SKIN_SUGGESTIONS)
                                                .executes { ctx ->
                                                    val target = EntityArgument.getPlayer(ctx, "target")
                                                    val skinId = StringArgumentType.getString(ctx, "skinId")
                                                    cmdAdminSet(ctx.source, target, skinId, 1)
                                                }
                                                .then(
                                                    Commands.argument("slot", IntegerArgumentType.integer(1, 6))
                                                        .executes { ctx ->
                                                            val target = EntityArgument.getPlayer(ctx, "target")
                                                            val skinId = StringArgumentType.getString(ctx, "skinId")
                                                            val slot = IntegerArgumentType.getInteger(ctx, "slot")
                                                            cmdAdminSet(ctx.source, target, skinId, slot)
                                                        }
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("clear")
                                .then(
                                    Commands.argument("target", EntityArgument.player())
                                        .executes { ctx ->
                                            val target = EntityArgument.getPlayer(ctx, "target")
                                            cmdAdminClear(ctx.source, target, 1)
                                        }
                                        .then(
                                            Commands.argument("slot", IntegerArgumentType.integer(1, 6))
                                                .executes { ctx ->
                                                    val target = EntityArgument.getPlayer(ctx, "target")
                                                    val slot = IntegerArgumentType.getInteger(ctx, "slot")
                                                    cmdAdminClear(ctx.source, target, slot)
                                                }
                                        )
                                )
                        )
                )
        )
    }

    private fun cmdSet(player: ServerPlayer, skinId: String, slot: Int): Int {
        if (skinId !in CobblemonSkinMod.registeredSkins) {
            player.sendSystemMessage(
                Component.literal("[CobblemonSkin] 皮肤 '$skinId' 不存在。使用 /pokemonskin list 查看可用皮肤。")
            )
            return 0
        }

        val party = Cobblemon.INSTANCE.getStorage().getParty(player)
        val pokemon = CobblemonSkinMod.getPartyPokemon(party, slot)

        if (pokemon == null) {
            player.sendSystemMessage(
                Component.literal("[CobblemonSkin] 槽位 $slot 没有精灵。")
            )
            return 0
        }

        CobblemonSkinMod.applySkin(pokemon, skinId)

        player.sendSystemMessage(
            Component.literal("[CobblemonSkin] 已将皮肤 '$skinId' 应用到槽位 $slot 的 ${pokemon.species.name}。")
        )
        return 1
    }

    private fun cmdClear(player: ServerPlayer, slot: Int): Int {
        val party = Cobblemon.INSTANCE.getStorage().getParty(player)
        val pokemon = CobblemonSkinMod.getPartyPokemon(party, slot)

        if (pokemon == null) {
            player.sendSystemMessage(
                Component.literal("[CobblemonSkin] 槽位 $slot 没有精灵。")
            )
            return 0
        }

        val activeSkin = CobblemonSkinMod.getActiveSkin(pokemon)
        if (activeSkin == null) {
            player.sendSystemMessage(
                Component.literal("[CobblemonSkin] 槽位 $slot 的精灵没有皮肤。")
            )
            return 0
        }

        CobblemonSkinMod.clearSkin(pokemon)

        player.sendSystemMessage(
            Component.literal("[CobblemonSkin] 已清除槽位 $slot 的 ${pokemon.species.name} 的皮肤 '$activeSkin'。")
        )
        return 1
    }

    private fun cmdList(source: CommandSourceStack): Int {
        val skins = CobblemonSkinMod.registeredSkins
        if (skins.isEmpty()) {
            source.sendSuccess({ Component.literal("[CobblemonSkin] 当前没有注册任何皮肤。") }, false)
            return 0
        }
        source.sendSuccess({
            Component.literal("[CobblemonSkin] 可用皮肤 (${skins.size}): ${skins.joinToString(", ")}")
        }, false)
        return 1
    }

    private fun cmdInfo(player: ServerPlayer, slot: Int): Int {
        val party = Cobblemon.INSTANCE.getStorage().getParty(player)
        val pokemon = CobblemonSkinMod.getPartyPokemon(party, slot)

        if (pokemon == null) {
            player.sendSystemMessage(
                Component.literal("[CobblemonSkin] 槽位 $slot 没有精灵。")
            )
            return 0
        }

        val activeSkin = CobblemonSkinMod.getActiveSkin(pokemon) ?: "无"
        player.sendSystemMessage(
            Component.literal("[CobblemonSkin] 槽位 $slot ${pokemon.species.name} 当前皮肤: $activeSkin")
        )
        return 1
    }

    private fun cmdAdminSet(source: CommandSourceStack, target: ServerPlayer, skinId: String, slot: Int): Int {
        if (skinId !in CobblemonSkinMod.registeredSkins) {
            source.sendFailure(
                Component.literal("[CobblemonSkin] 皮肤 '$skinId' 不存在。使用 /pokemonskin list 查看可用皮肤。")
            )
            return 0
        }

        val party = Cobblemon.INSTANCE.getStorage().getParty(target)
        val pokemon = CobblemonSkinMod.getPartyPokemon(party, slot)

        if (pokemon == null) {
            source.sendFailure(
                Component.literal("[CobblemonSkin] ${target.name.string} 的槽位 $slot 没有精灵。")
            )
            return 0
        }

        CobblemonSkinMod.applySkin(pokemon, skinId)

        source.sendSuccess({
            Component.literal("[CobblemonSkin] 已将皮肤 '$skinId' 应用到 ${target.name.string} 槽位 $slot 的 ${pokemon.species.name}。")
        }, true)
        target.sendSystemMessage(
            Component.literal("[CobblemonSkin] 管理员将皮肤 '$skinId' 应用到了你的槽位 $slot 的 ${pokemon.species.name}。")
        )
        return 1
    }

    private fun cmdAdminClear(source: CommandSourceStack, target: ServerPlayer, slot: Int): Int {
        val party = Cobblemon.INSTANCE.getStorage().getParty(target)
        val pokemon = CobblemonSkinMod.getPartyPokemon(party, slot)

        if (pokemon == null) {
            source.sendFailure(
                Component.literal("[CobblemonSkin] ${target.name.string} 的槽位 $slot 没有精灵。")
            )
            return 0
        }

        val activeSkin = CobblemonSkinMod.getActiveSkin(pokemon)
        if (activeSkin == null) {
            source.sendFailure(
                Component.literal("[CobblemonSkin] ${target.name.string} 槽位 $slot 的精灵没有皮肤。")
            )
            return 0
        }

        CobblemonSkinMod.clearSkin(pokemon)

        source.sendSuccess({
            Component.literal("[CobblemonSkin] 已清除 ${target.name.string} 槽位 $slot 的 ${pokemon.species.name} 的皮肤 '$activeSkin'。")
        }, true)
        target.sendSystemMessage(
            Component.literal("[CobblemonSkin] 管理员清除了你的槽位 $slot 的 ${pokemon.species.name} 的皮肤。")
        )
        return 1
    }
}
