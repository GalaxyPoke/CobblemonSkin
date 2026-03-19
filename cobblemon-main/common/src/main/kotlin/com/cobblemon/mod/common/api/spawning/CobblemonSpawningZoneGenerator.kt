/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.spawning.influence.SpawningZoneInfluence
import com.cobblemon.mod.common.api.spawning.influence.detector.SpawningInfluenceDetector
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.api.spawning.spawner.SpawningZoneInput
import com.cobblemon.mod.common.api.tags.CobblemonBlockTags
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos.blockToSectionCoord
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.status.ChunkStatus
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

/**
 * A spawning zone generator that takes a straightforward approach
 * in slicing out a [SpawningZone]. If you want to replace this,
 * change over the value of [Cobblemon.spawningZoneGenerator].
 *
 * @author Hiroku
 * @since February 5th, 2022
 */
object CobblemonSpawningZoneGenerator : SpawningZoneGenerator {
    override fun generate(
        spawner: Spawner,
        input: SpawningZoneInput
    ): SpawningZone {
        val world = input.world
        var baseY = input.baseY
        var height = input.height
        if (baseY < world.minBuildHeight) {
            val difference = world.minBuildHeight - baseY
            baseY += difference
            height -= difference
            if (height < 1) {
                throw IllegalStateException("World slice was attempted with totally awful base and dimensions")
            }
        }

        if (baseY + height >= world.maxBuildHeight) {
            val difference = baseY + height - 1 - world.maxBuildHeight
            height -= difference
            if (height < 1) {
                throw IllegalStateException("World slice was attempted with totally awful base and dimensions")
            }
        }

        val minimumDistanceBetweenEntities = config.minimumDistanceBetweenEntities
        val nearbyEntityPositions = input.world.getEntities(
            input.cause.entity,
            AABB.ofSize(
                Vec3(
                    input.baseX + input.length / 2.0,
                    baseY + height / 2.0,
                    input.baseZ + input.width / 2.0
                ),
                input.length + minimumDistanceBetweenEntities,
                height + minimumDistanceBetweenEntities,
                input.width + minimumDistanceBetweenEntities
            )
        ).filterIsInstance<LivingEntity>()
            .map { it.position() }

        val defaultState = Blocks.STONE.defaultBlockState()
        val defaultBlockData = SpawningZone.BlockData(defaultState, 0, 0)

        val blocks = Array(input.length) { Array(height) { Array(input.width) { defaultBlockData } } }
        val skyLevel = Array(input.length) { Array(input.width) { world.maxBuildHeight } }
        val pos = BlockPos.MutableBlockPos()
        val spawningZoneInfluences = mutableListOf<SpawningZoneInfluence>()

        val chunks = mutableMapOf<Pair<Int, Int>, ChunkAccess?>()
        val yRange = (baseY until baseY + height).reversed()
        val lightingProvider = world.lightEngine
        for (x in input.baseX until input.baseX + input.length) {
            for (z in input.baseZ until input.baseZ + input.width) {
                val query = chunks.computeIfAbsent(Pair(blockToSectionCoord(x), blockToSectionCoord(z))) {
                    world.getChunk(it.first, it.second, ChunkStatus.FULL, false)
                } ?: continue

                var canSeeSky = world.canSeeSkyFromBelowWater(pos.set(x, yRange.first, z))
                for (y in yRange) {
                    val skyLight = lightingProvider.getLayerListener(LightLayer.SKY).getLightValue(pos.set(x, y, z))
                    val state = query.getBlockState(pos.set(x, y, z))
                    blocks[x - input.baseX][y - baseY][z - input.baseZ] = SpawningZone.BlockData(
                        state = state,
                        light = world.getMaxLocalRawBrightness(pos),
                        skyLight = skyLight
                    )
                    spawningZoneInfluences.addAll(SpawningInfluenceDetector.detectors.flatMap { it.detectFromBlock(world, pos, state) })
                    if (canSeeSky) {
                        skyLevel[x - input.baseX][z - input.baseZ] = y
                    }
                    if (state.fluidState.isEmpty && !state.`is`(CobblemonBlockTags.SEES_SKY)) {
                        canSeeSky = false
                    }
                }
            }
        }

        for (spawningZoneGenerator in SpawningInfluenceDetector.detectors) {
            spawningZoneInfluences.addAll(spawningZoneGenerator.detectFromInput(spawner, input))
        }

        return SpawningZone(
            cause = input.cause,
            world = world,
            baseX = input.baseX,
            baseY = baseY,
            baseZ = input.baseZ,
            blocks = blocks,
            skyLevel = skyLevel,
            nearbyEntityPositions = nearbyEntityPositions,
            influences = spawningZoneInfluences
        )
    }
}