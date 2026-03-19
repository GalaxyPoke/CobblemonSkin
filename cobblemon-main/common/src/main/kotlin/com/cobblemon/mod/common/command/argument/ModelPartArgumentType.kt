/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command.argument

import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult.Type
import java.util.concurrent.CompletableFuture

class ModelPartArgumentType(): ArgumentType<String> {
    override fun parse(reader: StringReader): String {
        return reader.readString()
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(getModelParts(context).keys.toList(),builder)
    }

    companion object {
        fun modelPart() = ModelPartArgumentType()

        fun <S: Any> getModel(context: CommandContext<S> ): PosableModel? {
            if (context.source is SharedSuggestionProvider) {
                val targetEntity = (Minecraft.getInstance().hitResult as? EntityHitResult)
                    ?.takeIf { it.type == Type.ENTITY }
                    ?.entity
                //Could probably work for NPCs too with some changes
                if (targetEntity is PokemonEntity) {
                    val state = FloatingState()
                    state.currentAspects = targetEntity.aspects
                    return VaryingModelRepository.getPoser(name = targetEntity.exposedSpecies.resourceIdentifier, state = state)
                }
            }
            return null
        }

        private fun <S: Any> getModelParts(context: CommandContext<S>): MutableMap<String, ModelPart> {
            val modelParts = getModel(context)?.relevantPartsByName?:mutableMapOf()
            return modelParts
        }

        fun getModelPart(context: CommandContext<CommandSourceStack>, name: String): ModelPart? {
            val partName = context.getArgument(name, String::class.java )
            val part = getModelParts(context)[partName]
                ?: run {
                    context.source.playerOrException.sendSystemMessage("No joint named $partName".red())
                    return null
                }
            return part
        }

    }
}