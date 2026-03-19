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
import com.mojang.serialization.Codec
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import java.io.DataOutputStream
import java.io.FileOutputStream
import java.util.UUID

abstract class NbtBackedPlayerData<T : InstancedPlayerData>(
    subfolder: String,
    type: PlayerInstancedDataStoreType
) : FileBasedPlayerDataStoreBackend<T>(subfolder, type, "nbt") {
    abstract val codec: Codec<T>

    override fun save(playerData: T) {
        val fileTmp = filePath(playerData.uuid, TEMPORARY_FILE_EXTENSION)
        fileTmp.parentFile.mkdirs()
        val encodeResult = codec.encodeStart(NbtOps.INSTANCE, playerData)
        val tag = encodeResult.result().get() as CompoundTag
        Cobblemon.playerDataManager.saveExecutor.submit {
            NbtIo.write(tag, fileTmp.toPath())
            postSaveFileMoving(playerData.uuid)
        }
    }

    override fun load(uuid: UUID): T {
        return loadWithFallback(uuid) {
            val input = NbtIo.read(it.toPath())
            val decodeResult = codec.decode(NbtOps.INSTANCE, input)
            decodeResult.getOrThrow {
                Cobblemon.LOGGER.error("Error decoding $subfolder for player uuid $uuid")
                Cobblemon.LOGGER.error(it)
                throw UnsupportedOperationException()
            }.first
        }
    }
}