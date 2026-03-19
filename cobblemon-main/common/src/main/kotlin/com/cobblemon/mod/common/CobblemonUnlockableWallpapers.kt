/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.storage.pc.UnlockablePCWallpaper
import com.cobblemon.mod.common.util.adapters.ExpressionAdapter
import com.cobblemon.mod.common.util.adapters.ExpressionLikeAdapter
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.adapters.TextAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType

/**
 * A data registry for unlockable wallpapers that can be applied to a PC box. This is different to a resource packed
 * wallpaper, as those are purely client-side and don't require any unlocking.
 *
 * See docs/pc_wallpapers.md
 *
 * @author Hiroku
 * @since February 9th, 2025
 */
object CobblemonUnlockableWallpapers : JsonDataRegistry<UnlockablePCWallpaper> {
    override val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Expression::class.java, ExpressionAdapter)
        .registerTypeAdapter(ExpressionLike::class.java, ExpressionLikeAdapter)
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .registerTypeAdapter(Component::class.java, TextAdapter)
        .create()

    override val typeToken = TypeToken.get(UnlockablePCWallpaper::class.java)
    override val resourcePath = "unlockable_pc_box_wallpapers"
    override val id: ResourceLocation = cobblemonResource("unlockable_pc_box_wallpapers")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<CobblemonUnlockableWallpapers>()

    val unlockableWallpapers = mutableMapOf<ResourceLocation, UnlockablePCWallpaper>()

    override fun sync(player: ServerPlayer) { /* These don't sync, they're applied to a PC instance from which they're hooked into RequestChangePCBoxWallpaperHandler. */ }

    override fun reload(data: Map<ResourceLocation, UnlockablePCWallpaper>) {
        unlockableWallpapers.clear()
        data.forEach { (id, value) -> value.id = id }
        unlockableWallpapers.putAll(data)
        observable.emit(this)
    }
}