/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.storage

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/**
 * Event being fired whenever the client connects to a server and "requests" what wallpapers are available for it to choose from.
 * Mods can listen to this event and remove available options from the client when they connect, essentially causing them to not exist client-side
 *
 * NOTE: Adding wallpapers here that do not exist on the client, will result in the purple/black fallback texture being displayed client-side.
 *
 * @author JustAHuman-xD
 * @since February 14th, 2025
 */
data class WallpaperCollectionEvent(
    val player: ServerPlayer,
    val wallpapers: MutableSet<ResourceLocation>
)