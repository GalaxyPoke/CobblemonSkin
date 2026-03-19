/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player

import com.cobblemon.mod.common.Cobblemon.MODID
import com.cobblemon.mod.common.api.pokedex.PokedexManager
import com.cobblemon.mod.common.api.scheduling.ScheduledTask
import com.cobblemon.mod.common.api.scheduling.ServerTaskTracker
import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

/**
 * Manages all types of [InstancedPlayerData]
 * Essentially, we have multiple types of data attached to a player that we might want to save in different formats/files/folders/dbs
 * To add a new type, add a new type in [PlayerInstancedDataStoreType],
 * create corresponding [InstancedPlayerData] and [ClientInstancedPlayerData] classes
 * Then add the type associated with a [PlayerInstancedDataFactory] (probably a [CachedPlayerDataStoreFactory]
 *
 * @author Apion
 * @since February 21, 2024
 */

open class PlayerInstancedDataStoreManager {
    val saveExecutor = Executors.newSingleThreadExecutor(
        ThreadFactoryBuilder()
            .setNameFormat("$MODID Player Data Save Executor")
            .setDaemon(true)
            .setPriority(1)
            .build()
    )
    val factories = mutableMapOf<PlayerInstancedDataStoreType, PlayerInstancedDataFactory<*>>()
    val saveTasks = mutableMapOf<PlayerInstancedDataStoreType, ScheduledTask>()
    open fun setFactory(factory: PlayerInstancedDataFactory<*>, dataType: PlayerInstancedDataStoreType) {
        factories[dataType] = factory
    }


    open fun setup(server: MinecraftServer) {
        factories.values.forEach {
            it.setup(server)
        }
        //Should put this somewhere else
        saveTasks[PlayerInstancedDataStoreTypes.GENERAL] = ScheduledTask.Builder()
            .execute { saveAllOfOneType(PlayerInstancedDataStoreTypes.GENERAL) }
            .delay(30f)
            .interval(120f)
            .infiniteIterations()
            .tracker(ServerTaskTracker)
            .build()

        saveTasks[PlayerInstancedDataStoreTypes.POKEDEX] = ScheduledTask.Builder()
            .execute { saveAllOfOneType(PlayerInstancedDataStoreTypes.POKEDEX) }
            .delay(30f)
            .interval(120f)
            .infiniteIterations()
            .tracker(ServerTaskTracker)
            .build()
    }

    open fun get(playerId: UUID, dataType: PlayerInstancedDataStoreType): InstancedPlayerData {
        if (!factories.contains(dataType)) {
            throw UnsupportedOperationException("No factory registered for $dataType")
        }
        return factories[dataType]!!.getForPlayer(playerId)
    }

    open fun get(player: Player, dataType: PlayerInstancedDataStoreType): InstancedPlayerData {
        return get(player.uuid, dataType)
    }

    open fun saveAllOfOneType(dataType: PlayerInstancedDataStoreType) {
        if (!factories.contains(dataType)) {
            throw UnsupportedOperationException("No factory registered for $dataType")
        }
        return factories[dataType]!!.saveAll()
    }

    open fun saveSingle(playerData: InstancedPlayerData, dataType: PlayerInstancedDataStoreType) {
        if (!factories.contains(dataType)) {
            throw UnsupportedOperationException("No factory registered for $dataType")
        }
        return factories[dataType]!!.saveSingle(playerData.uuid)
    }

    open fun onPlayerDisconnect(player: ServerPlayer) {
        factories.values.forEach {
            it.onPlayerDisconnect(player)
        }
    }

    open fun syncAllToPlayer(player: ServerPlayer) {
        factories.values.forEach {
            it.sendToPlayer(player)
        }
    }

    open fun saveAllStores() {
        factories.values.forEach {
            it.saveAll()
        }
    }

    open fun getGenericData(player: ServerPlayer): GeneralPlayerData {
        return getGenericData(player.uuid)
    }

    open fun getGenericData(playerId: UUID): GeneralPlayerData {
        return get(playerId, PlayerInstancedDataStoreTypes.GENERAL) as GeneralPlayerData
    }

    open fun getPokedexData(player: ServerPlayer): PokedexManager {
        return getPokedexData(player.uuid)
    }

    open fun getPokedexData(playerId: UUID): PokedexManager {
        return get(playerId, PlayerInstancedDataStoreTypes.POKEDEX) as PokedexManager
    }
}