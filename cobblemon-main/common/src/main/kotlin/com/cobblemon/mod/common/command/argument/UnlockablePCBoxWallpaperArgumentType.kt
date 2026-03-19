/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command.argument

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonUnlockableWallpapers
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.asTranslated
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.resources.ResourceLocation

class UnlockablePCBoxWallpaperArgumentType : ArgumentType<ResourceLocation> {

    companion object {
        val EXAMPLES: List<String> = listOf("cobblemon:charcadet")
        val INVALID_WALLPAPER = "cobblemon.command.unlockablepcboxwallpaper.invalid-wallpaper".asTranslated()

        fun wallpaper() = UnlockablePCBoxWallpaperArgumentType()

        fun <S> getUnlockablePCBoxWallpaper(context: CommandContext<S>, name: String): ResourceLocation {
            return context.getArgument(name, ResourceLocation::class.java)
        }
    }

    override fun parse(reader: StringReader): ResourceLocation {
        try {
            return reader.asIdentifierDefaultingNamespace()
        } catch (e: Exception) {
            throw SimpleCommandExceptionType(INVALID_WALLPAPER).createWithContext(reader)
        }
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(CobblemonUnlockableWallpapers.unlockableWallpapers.keys.map { if (it.namespace == Cobblemon.MODID) it.path else it.toString() }, builder)
    }

    override fun getExamples() = EXAMPLES
}