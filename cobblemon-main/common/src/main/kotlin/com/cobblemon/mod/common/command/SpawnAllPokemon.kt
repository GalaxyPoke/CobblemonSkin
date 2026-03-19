/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.requiresWithPermission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.MobSpawnType

object SpawnAllPokemon {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("spawnallpokemon")
                .requiresWithPermission(CobblemonPermissions.SPAWN_ALL_POKEMON) { it.player != null }
                .then(
                    Commands.argument("min", IntegerArgumentType.integer(1))
                        .then(
                            Commands.argument("max", IntegerArgumentType.integer(1))
                                .executes {
                                    execute(it, IntegerArgumentType.getInteger(it, "min")..IntegerArgumentType.getInteger(it, "max"))
                                }
                        )
                        .executes { execute(it, IntegerArgumentType.getInteger(it, "min")..Int.MAX_VALUE) }
                )
                .executes { execute(it, 1..Int.MAX_VALUE) }
        )
    }

    private fun execute(context: CommandContext<CommandSourceStack>, range: IntRange) : Int {
        val player = context.source.playerOrException
        val world = context.source.level
        for (species in PokemonSpecies.implemented) {
            if (species.nationalPokedexNumber in range) {
                LOGGER.debug(species.name)
                val pokemonEntity = PokemonProperties.parse("species=${species.name} level=10").createEntity(context.source.level)
                val blockPos = player.blockPosition()
                pokemonEntity.moveTo(player.x, player.y, player.z, pokemonEntity.yRot, pokemonEntity.xRot)
                pokemonEntity.entityData.set(PokemonEntity.SPAWN_DIRECTION, pokemonEntity.random.nextFloat() * 360F)
                pokemonEntity.finalizeSpawn(world, world.getCurrentDifficultyAt(blockPos), MobSpawnType.COMMAND, null)
                context.source.level.addFreshEntity(pokemonEntity)
            }
        }

        return Command.SINGLE_SUCCESS
    }
}