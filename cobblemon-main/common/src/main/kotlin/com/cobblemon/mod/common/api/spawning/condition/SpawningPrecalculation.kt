/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.spawning.SpawnBucket
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import net.minecraft.resources.ResourceLocation

/**
 * A type of precalculation that can occur on a list of [SpawnDetail] to accelerate
 * checks on whether a spawn can occur at a particular [SpawnablePosition]. This process
 * works by creating a [HashMap] keying from some property of a [SpawnDetail] and
 * [SpawnablePosition] and mapping it to a list of spawns that were calculated ahead of
 * time as sharing that value. The requirement of implementations is only to get that
 * key from both a [SpawnDetail] and a [SpawnablePosition].
 *
 * When a precalculation has been registered with a [Spawner] by adding to [Spawner.precalculators],
 * the precalculation result will need to be updated by running [Spawner.precalculate]. That
 * might take it a hot minute, but the result will be that there is an extra layer of
 * shortcutting to the matching algorithm.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
interface SpawningPrecalculation<T : Any> {
    /** Retrieves the precalculation keys for the given spawn detail. */
    fun select(detail: SpawnDetail): Collection<T>
    /** Retrieves the precalculation key for the given spawnable position, if that key exists for this position. */
    fun select(bucket: SpawnBucket, spawnablePosition: SpawnablePosition): T?

    /**
     * Generates a precalculation result for the list of [SpawnDetail]s and the subsequent precalculations.
     *
     * This either returns a [NestedPrecalculationResult] (when there are some later precalculations to do)
     * or a [FinalPrecalculationResult] if there are no further precalculations to do.
     *
     * This function is vaguely recursive but unless the function itself is bugged then it won't overflow.
     */
    fun generate(details: List<SpawnDetail>, next: List<SpawningPrecalculation<*>>): PrecalculationResult<T> {
        val mapping = details
            .filter { select(it).isNotEmpty() }
            .flatMap { detail -> select(detail).map { it to detail } }
            .groupBy({ it.first }, { it.second })
            .toMap()

        if (next.isEmpty()) {
            return FinalPrecalculationResult(calculation = this, mapping = mapping)
        } else {
            val immediateNext = next.first()
            val subNext = next.subList(1, next.size)
            return NestedPrecalculationResult(
                calculation = this,
                mapping = mapping.entries.associate { it.key to immediateNext.generate(it.value, subNext) }
            )
        }
    }
}

/**
 * A dummy precalculation that doesn't actually do any precalculating. Used for situations
 * where there weren't any precalculators registered.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
object RootPrecalculation : SpawningPrecalculation<Any> {
    val list = listOf(Unit)
    override fun select(detail: SpawnDetail): List<Any> = list
    override fun select(bucket: SpawnBucket, spawnablePosition: SpawnablePosition): Any = Unit
}

/**
 * A precalculation based on what spawnable position type a spawn can be. This is a pretty sweet precalc.
 *
 * @author Hiroku
 * @since February 14th, 2022
 */
object SpawnablePositionTypePrecalculation : SpawningPrecalculation<Class<out SpawnablePosition>> {
    override fun select(detail: SpawnDetail) = listOf(detail.spawnablePositionType.clazz)
    override fun select(bucket: SpawnBucket, spawnablePosition: SpawnablePosition) = spawnablePosition::class.java
}

object BucketPrecalculation : SpawningPrecalculation<SpawnBucket> {
    override fun select(detail: SpawnDetail) = listOf(detail.bucket)
    override fun select(bucket: SpawnBucket, spawnablePosition: SpawnablePosition) = bucket
}

/**
 * This isn't so much of a precalculation as it is a yoinking of another precalculation.
 * Uses pre-established [SpawnDetail.validBiomes] to shortcut knowing which spawns are valid for a biome.
 */
object BiomePrecalculation : SpawningPrecalculation<ResourceLocation> {
    override fun select(detail: SpawnDetail) = detail.validBiomes
    override fun select(bucket: SpawnBucket, spawnablePosition: SpawnablePosition) = spawnablePosition.biomeHolder.unwrapKey().map { it.location() }.orElse(null)
}

/**
 * The result of a precalculation. This is mostly just a delegate so that the next
 * result of a precalculation can either be an actual list of spawns or merely another
 * layer of precalculation.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
sealed class PrecalculationResult<T : Any>(
    val calculation: SpawningPrecalculation<*>
) {
    /** Gets the list of spawn details that fit this spawnable position. */
    abstract fun retrieve(bucket: SpawnBucket, spawnablePosition: SpawnablePosition): List<SpawnDetail>
}

/**
 * A precalculation result when there was another precalculation made on the result.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
class NestedPrecalculationResult<T : Any>(
    calculation: SpawningPrecalculation<T>,
    val mapping: Map<T, PrecalculationResult<*>> = mutableMapOf()
) : PrecalculationResult<T>(calculation) {
    override fun retrieve(bucket: SpawnBucket, spawnablePosition: SpawnablePosition) = mapping[calculation.select(bucket, spawnablePosition)]?.retrieve(bucket, spawnablePosition) ?: emptyList()
}

/**
 * The final stage of a precalculation where there is a final mapping from the key
 * to a list of spawns. Every precalculation tree ends in this type of result.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
class FinalPrecalculationResult<T : Any>(
    calculation: SpawningPrecalculation<*>,
    val mapping: Map<T, List<SpawnDetail>>
) : PrecalculationResult<T>(calculation) {
    override fun retrieve(bucket: SpawnBucket, spawnablePosition: SpawnablePosition) = mapping[calculation.select(bucket, spawnablePosition)] ?: emptyList()
}