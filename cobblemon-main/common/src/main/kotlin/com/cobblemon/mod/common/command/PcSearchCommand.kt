/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.text.*
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType
import com.cobblemon.mod.common.util.*
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import com.cobblemon.mod.common.api.pokemon.stats.Stats

object PcSearchCommand {

    private const val NAME = "pcsearch"
    private const val PROPERTIES = "properties"
    private const val OTHER = "other"
    private val IN_BATTLE_EXCEPTION = SimpleCommandExceptionType(lang("pc.inbattle").red())

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            literal(NAME)
                .permission(CobblemonPermissions.PC_SEARCH)
                .then(
                    argument(PROPERTIES, PokemonPropertiesArgumentType.properties())
                        .executes { execute(it, it.source.playerOrException) }
                )
                .then(
                    literal("other")
                        .then(
                            argument(OTHER, EntityArgument.player())
                                .then(
                                    argument(PROPERTIES, PokemonPropertiesArgumentType.properties())
                                        .executes { execute(it, EntityArgument.getPlayer(it, OTHER)) }
                                )
                        )
                )
        )
    }

    private fun formatField(label: MutableComponent, value: MutableComponent): MutableComponent {
        return label.add(": ").aqua().add(value.yellow())
    }

    private fun execute(context: CommandContext<CommandSourceStack>, player: ServerPlayer): Int {
        if (player.isInBattle()) {
            throw IN_BATTLE_EXCEPTION.create()
        }

        val pokemonProperties = PokemonPropertiesArgumentType.getPokemonProperties(context, PROPERTIES)
        
        val pc = player.pc()
        val matchingPokemon = mutableListOf<MutableComponent>()

        pc.boxes.forEachIndexed { boxIndex, box ->
            val nonEmptySlots = box.getNonEmptySlots()
            
            for ((slotIndex, pokemon) in nonEmptySlots) {
                if (pokemonProperties.matches(pokemon)) {
                    val boxNumber = boxIndex + 1
                    val displayName = pokemon.species.translatedName
                    val level = pokemon.level

                    val baseText = commandLang("pcsearch.entry", displayName, lang("label.lv", level)).yellow()

                    val hoverText = Component.literal("")
                        .add(formatField(lang("ui.info.species"), displayName))
                        .add(" ").add(lang("label.lv", level))
                        .add("\n").add(formatField(lang("ui.info.gender"), lang("gender.${pokemon.gender.name.lowercase()}")))
                        .add("\n").add(formatField(lang("ui.info.nature"), pokemon.nature.displayName.asTranslated()))
                        .add("\n").add(formatField(lang("ui.info.ability"), pokemon.ability.displayName.asTranslated()))
                        .add("\n").add(lang("ui.stats.ivs").add(": ").aqua().add(
                            "${pokemon.ivs[Stats.HP]}/${pokemon.ivs[Stats.ATTACK]}/${pokemon.ivs[Stats.DEFENCE]}/${pokemon.ivs[Stats.SPEED]}/${pokemon.ivs[Stats.SPECIAL_ATTACK]}/${pokemon.ivs[Stats.SPECIAL_DEFENCE]}".yellow()
                        ))
                        .add("\n").add(lang("ui.stats.evs").add(": ").aqua().add(
                            "${pokemon.evs[Stats.HP]}/${pokemon.evs[Stats.ATTACK]}/${pokemon.evs[Stats.DEFENCE]}/${pokemon.evs[Stats.SPEED]}/${pokemon.evs[Stats.SPECIAL_ATTACK]}/${pokemon.evs[Stats.SPECIAL_DEFENCE]}".yellow()
                        ))
                    
                    if (pokemon.shiny) {
                        hoverText.add("\n").add(commandLang("pcsearch.tooltip.shiny").gold())
                    }
                    
                    baseText.onHover(hoverText)

                    val locationText = commandLang("pcsearch.location", boxNumber, slotIndex + 1).green()
                        .onHover(commandLang("pcsearch.takepokemon"))
                        .suggest("/pctake ${player.name.string} $boxNumber ${slotIndex + 1}")
                    
                    matchingPokemon.add(baseText.add(locationText))
                }
            }
        }

        if (matchingPokemon.isEmpty()) {
            context.source.sendSystemMessage(commandLang("pcsearch.nomatch", pokemonProperties.originalString, player.name.string))
        } else {
            context.source.sendSystemMessage(commandLang("pcsearch.found", player.name.string))
            matchingPokemon.forEach { result ->
                context.source.sendSystemMessage(result)
            }
        }

        return Command.SINGLE_SUCCESS
    }
}