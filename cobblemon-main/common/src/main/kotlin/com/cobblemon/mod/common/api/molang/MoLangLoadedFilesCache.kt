/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.molang

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.struct.VariableStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.Cobblemon
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource

/**
 * A cache for loaded MoLang files to avoid redundant file I/O operations.
 * Provides functions to load, save, check existence, and clear cached files.
 *
 * Only files with /molang/ in their path and a .json extension within the config
 * or data folders can be accessed.
 *
 * @author Hiroku
 * @since November 1st, 2025
 */
object MoLangLoadedFilesCache {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    val loadedFiles = ConcurrentHashMap<String, VariableStruct>()

    /** Essentially the server root or world root for singleplayer. */
    lateinit var baseFolder: Path
    private lateinit var configFolder: Path
    private lateinit var dataFolder: Path

    fun initialize(server: MinecraftServer) {
        baseFolder = server.getWorldPath(LevelResource.ROOT)
        configFolder = baseFolder.resolve("config").normalize().toAbsolutePath()
        dataFolder = baseFolder.resolve("data").normalize().toAbsolutePath()
    }
    
    val struct = QueryStruct(hashMapOf())
        .addFunction("load") { params ->
            val fileName = params.params[0].asString()
            return@addFunction load(fileName)
        }
        .addFunction("save") { params ->
            val fileName = params.params[0].asString()
            val data = params.params[1] as VariableStruct
            save(fileName, data)
            return@addFunction DoubleValue.ONE
        }
        .addFunction("exists") { params ->
            val fileName = params.params[0].asString()
            val exists = exists(fileName)
            return@addFunction if (exists) DoubleValue.ONE else DoubleValue.ZERO
        }
        .addFunction("clear") { params ->
            val fileName = params.params[0].asString()
            clear(fileName)
            return@addFunction DoubleValue.ONE
        }

    private fun validatePath(fileName: String): Path? {
        val filePath = baseFolder.resolve(fileName).normalize().toAbsolutePath()
        if (!filePath.startsWith(configFolder) && !filePath.startsWith(dataFolder)) {
            Cobblemon.LOGGER.error("MoLang attempted to access file outside of config or data folder: $fileName")
            return null
        } else if ("/molang/" !in fileName) {
            Cobblemon.LOGGER.error("MoLang attempted to access file outside of a 'molang' subfolder: $fileName")
            return null
        } else if (!fileName.endsWith(".json")) {
            Cobblemon.LOGGER.error("MoLang attempted to access file without a .json extension: $fileName")
            return null
        }
        return filePath
    }

    fun exists(fileName: String): Boolean {
        val filePath = validatePath(fileName) ?: return false
        return filePath.toFile().exists()
    }

    fun clear(fileName: String) {
        loadedFiles.remove(fileName)
    }

    fun load(fileName: String): VariableStruct {
        return loadedFiles.getOrPut(fileName) {
            val filePath = validatePath(fileName) ?: return VariableStruct()
            val file = filePath.toFile()
            if (!file.exists()) {
                return VariableStruct()
            }
            val jsonElement = gson.fromJson(file.readText(), JsonElement::class.java)
            MoValue.of(jsonElement) as VariableStruct
        }
    }

    fun save(fileName: String, data: VariableStruct) {
        loadedFiles[fileName] = data
        val filePath = validatePath(fileName) ?: return
        val file = filePath.toFile()
        file.parentFile?.mkdirs()
        val jsonElement = MoValue.writeToJson(data)
        file.writeText(gson.toJson(jsonElement))
    }
}