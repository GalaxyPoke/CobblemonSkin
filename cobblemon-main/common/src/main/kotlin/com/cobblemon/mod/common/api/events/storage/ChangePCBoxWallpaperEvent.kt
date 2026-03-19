/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.storage

import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.storage.pc.PCBox
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * Events fired whenever a player changes their PC Wallpaper.
 * Has a cancelable [Pre] event and a [Post] which gets fired after the change.
 *
 * @author JustAHuman-xD
 * @since February 14th, 2025
 */
interface ChangePCBoxWallpaperEvent {

    /**
     * The [ServerPlayer] who is changing their wallpaper
     */
    val player: ServerPlayer

    /**
     * The [PCBox] whose wallpaper is being changed
     */
    val box: PCBox

    /**
     * The location of the wallpaper that is being changed to. Can be modified in the [Pre] event.
     * NOTE: Changing this to a wallpaper that does not exist on the client, will result in a default fallback texture being displayed.
     */
    val wallpaper: ResourceLocation

    /**
     * The location of the alternative wallpaper that is being changed to if available. Can be modified in the [Pre] event.
     * NOTE: Changing this to a wallpaper that does not exist on the client, will result in a default fallback texture being displayed.
     */
    val altWallpaper: ResourceLocation?

    class Pre(
        override val player: ServerPlayer,
        override val box: PCBox,
        override var wallpaper: ResourceLocation,
        override var altWallpaper: ResourceLocation?

    ) : ChangePCBoxWallpaperEvent, Cancelable() {
        val context = mutableMapOf(
            "player" to player.asMoLangValue(),
            "box" to StringValue(box.toString()),
            "wallpaper" to StringValue(wallpaper.toString()),
            "altWallpaper" to StringValue(altWallpaper?.toString())
        )
        val functions = mapOf(
            cancelFunc
        )
    }

    class Post(
        override val player: ServerPlayer,
        override val box: PCBox,
        override val wallpaper: ResourceLocation,
        override val altWallpaper: ResourceLocation?
    ) : ChangePCBoxWallpaperEvent {
        val context = mutableMapOf(
            "player" to player.asMoLangValue(),
            "box" to StringValue(box.toString()),
            "wallpaper" to StringValue(wallpaper.toString()),
            "altWallpaper" to StringValue(altWallpaper?.toString())
        )
    }
}