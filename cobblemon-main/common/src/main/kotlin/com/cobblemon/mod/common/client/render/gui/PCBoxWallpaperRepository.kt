/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.gui

import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.endsWith
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager

object PCBoxWallpaperRepository {
    lateinit var allWallpapers: Set<Triple<ResourceLocation, ResourceLocation?, ResourceLocation?>>
    lateinit var availableWallpapers: MutableSet<ResourceLocation>
    val defaultWallpaper = cobblemonResource("textures/gui/pc/wallpaper/basic/wallpaper_basic_05.png")

    fun findWallpapers(resourceManager: ResourceManager) {
        // Wallpaper resource, alternate wallpaper resource, and glow resource, if available as a triple
        val resources = mutableListOf<Triple<ResourceLocation, ResourceLocation?, ResourceLocation?>>()
        val wallpapers = mutableListOf<ResourceLocation>()

        val wallpaperPathList = mutableListOf<Pair<String, ResourceLocation>>()
        val altWallpaperPathList = mutableListOf<Pair<String, ResourceLocation>>()
        val wallpaperGlowPathList = mutableListOf<Pair<String, ResourceLocation>>()

        resourceManager.listResources("textures/gui/pc/wallpaper") { path -> path.endsWith(".png") }.keys.forEach { filePath ->
            val splitPath = filePath.toString().split("/")
            val fileName = splitPath[splitPath.lastIndex]
            val directory = splitPath[splitPath.lastIndex - 1]
            if (directory == "glow") {
                wallpaperGlowPathList.add(Pair(fileName, filePath))
            } else if (directory == "alt") {
                altWallpaperPathList.add(Pair(fileName, filePath))
            } else {
                wallpaperPathList.add(Pair(fileName, filePath))
            }
        }

        for (resource in wallpaperPathList) {
            // Find matching alternate wallpaper resource and glow resource if available
            val glowResource = wallpaperGlowPathList.find { it.first == resource.first }
            val altResource = altWallpaperPathList.find { it.first == resource.first }
            resources.add(Triple(resource.second, altResource?.second, glowResource?.second))
            wallpapers.add(resource.second)
        }

        allWallpapers = resources.toSet()
        availableWallpapers = wallpapers.toMutableSet()
    }
}
