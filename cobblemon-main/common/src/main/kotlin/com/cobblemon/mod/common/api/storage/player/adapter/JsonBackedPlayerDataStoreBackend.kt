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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

abstract class JsonBackedPlayerDataStoreBackend<T : InstancedPlayerData>(
    subfolder: String,
    type: PlayerInstancedDataStoreType
) : FileBasedPlayerDataStoreBackend<T>(subfolder, type, "json") {
    abstract val gson: Gson
    //The class GSON needs to deserialize to
    abstract val classToken: TypeToken<T>

    override fun save(playerData: T) {
        val fileTmp = filePath(playerData.uuid, TEMPORARY_FILE_EXTENSION)
        fileTmp.parentFile.mkdirs()
        val json = gson.toJson(playerData)
        Cobblemon.playerDataManager.saveExecutor.execute {
            fileTmp.printWriter().use { pw -> pw.write(json) }
            postSaveFileMoving(playerData.uuid)
        }
    }

    override fun load(uuid: UUID): T {
        return loadWithFallback(uuid) {
            it.reader().use { reader ->
                gson.fromJson(reader, classToken).also {
                    // Resolves old data that's missing new properties
                    val newProps = it::class.memberProperties.filterIsInstance<KMutableProperty<*>>().filter { member -> member.getter.call(it) == null }
                    if (newProps.isNotEmpty()) {
                        val defaultData = defaultData(uuid)
                        newProps.forEach { member -> member.setter.call(it, member.getter.call(defaultData)) }
                    }
                }
            }
        }
    }
}