/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.spawning.MoonPhaseRange
import com.cobblemon.mod.common.api.spawning.TimeRange
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.util.Merger
import com.cobblemon.mod.common.util.math.orMax
import com.cobblemon.mod.common.util.math.orMin
import com.mojang.datafixers.util.Either
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.WorldgenRandom
import net.minecraft.world.level.levelgen.structure.Structure

/**
 * The root of spawning conditions that can be applied to a spawnable position. What type
 * of spawnable position it can be applied to is relevant for any subclasses.
 *
 * @author Hiroku
 * @since January 24th, 2022
 */
abstract class SpawningCondition<T : SpawnablePosition> {
    companion object {
        val conditionTypes = mutableMapOf<String, Class<out SpawningCondition<*>>>()
        fun getByName(name: String) = conditionTypes[name]
        fun <T : SpawnablePosition, C : SpawningCondition<T>> register(name: String, clazz: Class<C>) {
            conditionTypes[name] = clazz
        }
    }

    var dimensions: MutableList<ResourceLocation>? = null
    /** This gets checked in a precalculation but still needs to be checked for things like rarity multipliers. */
    var biomes: MutableSet<RegistryLikeCondition<Biome>>? = null
    var moonPhase: MoonPhaseRange? = null
    var canSeeSky: Boolean? = null
    var minX: Float? = null
    var minY: Float? = null
    var minZ: Float? = null
    var maxX: Float? = null
    var maxY: Float? = null
    var maxZ: Float? = null
    var minLight: Int? = null
    var maxLight: Int? = null
    var minSkyLight: Int? = null
    var maxSkyLight: Int? = null
    var isRaining: Boolean? = null
    var isThundering: Boolean? = null
    var timeRange: TimeRange? = null
    var structures: MutableList<Either<ResourceLocation, TagKey<Structure>>>? = null
    var isSlimeChunk: Boolean? = null
    var markers: MutableList<String>? = null

    @Transient
    var appendages = mutableListOf<AppendageCondition>()

    abstract fun spawnablePositionClass(): Class<out T>
    fun spawnablePositionMatches(spawnablePosition: SpawnablePosition) = spawnablePositionClass().isAssignableFrom(spawnablePosition::class.java)

    fun isSatisfiedBy(spawnablePosition: SpawnablePosition): Boolean {
        return if (spawnablePositionMatches(spawnablePosition)) {
            fits(spawnablePosition as T)
        } else {
            false
        }
    }

    protected open fun fits(spawnablePosition: T): Boolean {
        if (spawnablePosition.position.x < minX.orMin() || spawnablePosition.position.x > maxX.orMax()) {
            return false
        } else if (spawnablePosition.position.y < minY.orMin() || spawnablePosition.position.y > maxY.orMax()) {
            return false
        } else if (spawnablePosition.position.z < minZ.orMin() || spawnablePosition.position.z > maxZ.orMax()) {
            return false
        } else if (moonPhase != null && spawnablePosition.moonPhase !in moonPhase!!) {
            return false
        } else if (spawnablePosition.light > maxLight.orMax() || spawnablePosition.light < minLight.orMin()) {
            return false
        } else if (spawnablePosition.skyLight > maxSkyLight.orMax() || spawnablePosition.skyLight < minSkyLight.orMin()) {
            return false
        } else if (timeRange != null && !timeRange!!.contains((spawnablePosition.world.dayTime() % 24000).toInt())) {
            return false
        } else if (canSeeSky != null && canSeeSky != spawnablePosition.canSeeSky) {
            return false
        } else if (isRaining != null && spawnablePosition.world.isRaining != isRaining!!) {
            return false
        } else if (isThundering != null && spawnablePosition.world.isThundering != isThundering!!) {
            return false
        } else if (dimensions != null && dimensions!!.isNotEmpty() && spawnablePosition.world.dimension().location() !in dimensions!!) {
            return false
        } else if (markers != null && markers!!.isNotEmpty() && markers!!.none { marker -> marker in spawnablePosition.markers }) {
            return false
        } else if (biomes != null && biomes!!.isNotEmpty() && biomes!!.none { condition -> condition.fits(spawnablePosition.biomeHolder) }) {
            return false
        } else if (appendages.any { !it.fits(spawnablePosition) }) {
            return false
        } else if (structures != null && structures!!.isNotEmpty() &&
            structures!!.let { structures ->
                val structureAccess = spawnablePosition.world.structureManager()
                val cache = spawnablePosition.getStructureCache(spawnablePosition.position)
                return@let structures.none {
                    it.map({ cache.check(structureAccess, spawnablePosition.position, it) }, { cache.check(structureAccess, spawnablePosition.position, it) })
                }
            }
        ) {
            return false
        } else if (isSlimeChunk != null && isSlimeChunk != false) {
            val isSlimeChunk = WorldgenRandom.seedSlimeChunk(spawnablePosition.position.x shr 4, spawnablePosition.position.z shr 4, spawnablePosition.world.seed, 987234911L).nextInt(10) == 0

            if (!isSlimeChunk) {
                return false
            }

            /*val chunkX = spawnablePosition.position.x shr 4
            val chunkZ = spawnablePosition.position.z shr 4

            val seed = (spawnablePosition.world.seed +
                    (chunkX * chunkX * 4987142L) + (chunkX * 5947611L) +
                    (chunkZ * chunkZ * 4392871L) + (chunkZ * 389711L)) xor 987234911L

            val random = Random(seed)
            return random.nextInt(10) == 0*/
        }

        return true
    }

    open fun copyFrom(other: SpawningCondition<*>, merger: Merger) {
        dimensions = merger.merge(dimensions, other.dimensions)?.toMutableList()
        biomes = merger.merge(biomes, other.biomes)?.toMutableSet()
        moonPhase = merger.mergeSingle(moonPhase, other.moonPhase)
        canSeeSky = merger.mergeSingle(canSeeSky, other.canSeeSky)
        minX = merger.mergeSingle(minX, other.minX)
        minY = merger.mergeSingle(minY, other.minY)
        minZ = merger.mergeSingle(minZ, other.minZ)
        maxX = merger.mergeSingle(maxX, other.maxX)
        maxY = merger.mergeSingle(maxY, other.maxY)
        maxZ = merger.mergeSingle(maxZ, other.maxZ)
        minLight = merger.mergeSingle(minLight, other.minLight)
        maxLight = merger.mergeSingle(maxLight, other.maxLight)
        minSkyLight = merger.mergeSingle(minSkyLight, other.minSkyLight)
        maxSkyLight = merger.mergeSingle(maxSkyLight, other.maxSkyLight)
        timeRange = merger.mergeSingle(timeRange, other.timeRange)
        structures = merger.merge(structures, other.structures)?.toMutableList()
    }

    open fun isValid(): Boolean {
        if (biomes != null && biomes!!.any { it == null })
            return false
        if (dimensions != null && dimensions!!.any { it == null })
            return false
        if (structures != null && structures!!.any { it == null })
            return false
        return true
    }
}
