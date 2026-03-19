/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.adapter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.storage.player.InstancedPlayerData
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreType
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import java.io.File
import java.nio.file.Path
import java.util.UUID

/**
 * A [PlayerDataStoreBackend] that stores the [InstancedPlayerData] in a file.
 *
 * @author Apion
 * @since February 21, 2024
 */
abstract class FileBasedPlayerDataStoreBackend<T : InstancedPlayerData>(
    val subfolder: String,
    val type: PlayerInstancedDataStoreType,
    val fileExt: String
) : PlayerDataStoreBackend<T>(type) {

    abstract val defaultData: (UUID) -> (T)
    lateinit var savePath: Path
    val useNestedStructure = true

    override fun setup(server: MinecraftServer) {
        savePath = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).parent
    }

    protected fun postSaveFileMoving(uuid: UUID) {
        val tempFile = filePath(uuid, TEMPORARY_FILE_EXTENSION)
        val oldFile = filePath(uuid, OLD_FILE_EXTENSION)
        val file = filePath(uuid)
        if (file.exists()) {
            file.copyTo(oldFile, overwrite = true)
        }
        tempFile.copyTo(file, overwrite = true)
        tempFile.delete()
    }

    protected fun loadWithFallback(uuid: UUID, loadFunction: (File) -> T): T {
        val playerFile = filePath(uuid)
        val playerFileOld = filePath(uuid, OLD_FILE_EXTENSION)
        playerFile.parentFile.mkdirs()
        val loadFallback: () -> T = {
            playerFile.delete()
            if (playerFileOld.exists() && playerFileOld.length() > 0L) {
                try {
                    var result = loadFunction(playerFileOld)
                    playerFileOld.copyTo(playerFile)
                    Cobblemon.LOGGER.debug("Loaded .old {} for {}", subfolder, uuid)
                    result
                } catch (e: Exception) {
                    Cobblemon.LOGGER.error("Failed to load .old $subfolder for $uuid due to ${e.message}. Data is lost")
                    Cobblemon.LOGGER.error(e)
                    playerFileOld.delete()
                    defaultData.invoke(uuid).also(::save)
                }
            }
            else {
                Cobblemon.LOGGER.error(".old File $subfolder for $uuid is corrupt or missing. Data is lost")
                playerFileOld.delete()
                defaultData.invoke(uuid).also(::save)
            }
        }
        return if (playerFile.exists() && playerFile.length() > 0L) {
            try {
                loadFunction(playerFile)
            }
            catch (e: Exception) {
                Cobblemon.LOGGER.warn("Failed to load $subfolder for $uuid due to ${e.message}")
                Cobblemon.LOGGER.warn(e)
                loadFallback()
            }
        } else {
            loadFallback()
        }.also { it.initialize() }
    }

    @JvmOverloads
    fun getSubFile(uuid: UUID, extension: String? = null): String {
        return if (useNestedStructure) {
            "${uuid.toString().substring(0, 2)}/$uuid.$fileExt${if (extension != null) ".$extension" else ""}"
        } else {
            "$uuid.$fileExt.${if (extension != null) ".$extension" else ""}"
        }
    }

    @JvmOverloads
    fun filePath(uuid: UUID, extension: String? = null): File = savePath.resolve("$subfolder/${getSubFile(uuid, extension)}").toFile()

    companion object {
        const val OLD_FILE_EXTENSION = "old"
        const val TEMPORARY_FILE_EXTENSION = "tmp"
    }
}