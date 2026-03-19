/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command.argument

import com.cobblemon.mod.common.api.mark.Mark
import com.cobblemon.mod.common.api.mark.Marks
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.concurrent.CompletableFuture

class MarkArgumentType: ArgumentType<Mark> {

    override fun parse(reader: StringReader): Mark {
        try {
            return reader.asIdentifierDefaultingNamespace().let { Marks.getByIdentifier(it) } ?: throw Exception()
        } catch (e: Exception) {
            throw SimpleCommandExceptionType(INVALID_MARK).createWithContext(reader)
        }
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(Marks.identifiers(), builder)
    }

    override fun getExamples() = EXAMPLES

    companion object {

        val EXAMPLES: List<String> = listOf("cobblemon:ribbon_event")
        val INVALID_MARK: MutableComponent = Component.translatable("cobblemon.command.mark.invalid")

        fun mark() = MarkArgumentType()

        fun <S> getMark(context: CommandContext<S>, name: String): Mark {
            return context.getArgument(name, Mark::class.java)
        }
    }
}
