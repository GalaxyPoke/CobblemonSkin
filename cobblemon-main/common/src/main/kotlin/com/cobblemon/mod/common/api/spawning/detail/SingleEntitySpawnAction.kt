/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.detail

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.entity.SpawnEvent
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.MobSpawnType

/**
 * A spawn action that spawns a single entity. This is the most common type of spawn action.
 *
 * @author Hiroku
 * @since January 13th, 2024
 */
abstract class SingleEntitySpawnAction<T : Entity>(
    spawnablePosition: SpawnablePosition,
    bucket: SpawnBucket,
    detail: SpawnDetail
) : SpawnAction<EntitySpawnResult>(spawnablePosition, bucket, detail) {
    abstract fun createEntity(): T?

    override fun run(): EntitySpawnResult? {
        val e = createEntity() ?: return null
        e.setPos(spawnablePosition.position.toVec3d().add(0.5, 1.0, 0.5))
        var shouldSpawn = false
        CobblemonEvents.ENTITY_SPAWN.postThen(SpawnEvent(e, spawnablePosition), ifSucceeded = {
            this.entity.emit(e)
            if (e is Mob) {
                e.finalizeSpawn(spawnablePosition.world, spawnablePosition.world.getCurrentDifficultyAt(spawnablePosition.position), MobSpawnType.NATURAL, null)
            }
            spawnablePosition.world.addFreshEntity(e)
            shouldSpawn = true
        })

        return if (shouldSpawn) {
            EntitySpawnResult(listOf(e))
        } else null
    }


    /**
     * An observable for the entity that will eventually be spawned by this action.
     *
     * You can safely register subscriptions here for anything you want to do to the
     * entity after it's spawned, whenever that may be. This is tolerant of situations
     * where the entity spawn has been canceled, so that your action will not occur.
     */
    val entity: SimpleObservable<T> = SimpleObservable<T>().apply {
        subscribe { entity -> spawnablePosition.afterSpawn(action = this@SingleEntitySpawnAction, entity = entity) }
    }
}