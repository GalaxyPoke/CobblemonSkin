/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.position

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.spawning.SpawningZone
import com.cobblemon.mod.common.api.spawning.position.calculators.AreaSpawnablePositionCalculator
import com.cobblemon.mod.common.api.spawning.position.calculators.AreaSpawningInput
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.util.toVec3d
import net.minecraft.core.BlockPos

/**
 * Interface responsible for drawing a list of spawnable positions from a slice of the world,
 * given a list of all the spawnable position calculators that should be considered in order. As soon
 * as one of the spawnable position calculators returns true for [AreaSpawnablePositionCalculator.fits],
 * no other spawnable position calculator will be considered.
 *
 * The default method body of this interface checks every single block in the slice
 * and composes a single spawnable position per BlockPos, at most. This is almost certainly fine,
 * but this interface exists, so you can override it if you want.
 *
 * @author Hiroku
 * @since January 31st, 2022
 */
interface AreaSpawnablePositionResolver {
    fun resolve(
        spawner: Spawner,
        spawnablePositionCalculators: List<AreaSpawnablePositionCalculator<*>>,
        zone: SpawningZone
    ): List<AreaSpawnablePosition> {
        var pos = BlockPos.MutableBlockPos(1, 2, 3)
        val input = AreaSpawningInput(spawner, pos, zone)
        val spawnablePositions = mutableListOf<AreaSpawnablePosition>()

        var x = zone.baseX
        var y = zone.baseY
        var z = zone.baseZ

        while (x < zone.baseX + zone.length) {
            while (y < zone.baseY + zone.height) {
                while (z < zone.baseZ + zone.width) {
                    pos.set(x, y, z)
                    val vec = pos.toVec3d()
                    if (zone.nearbyEntityPositions.none { it.closerThan(vec, config.minimumDistanceBetweenEntities) && it != zone.cause.entity }) {
                        val fittedSpawnablePositionCalculator = spawnablePositionCalculators
                            .firstOrNull { calc -> calc.fits(input) && input.spawner.influences.none { !it.isAllowedPosition(input.world, input.position, calc) } }
                        if (fittedSpawnablePositionCalculator != null) {
                            val spawnablePosition = fittedSpawnablePositionCalculator.calculate(input)
                            if (spawnablePosition != null) {
                                val influences = zone.getInfluences(spawnablePosition)
                                for (influence in influences) {
                                    spawnablePosition.influences.add(influence)
                                    influence.affectSpawnablePosition(spawnablePosition)
                                }
                                for (influence in spawner.influences) {
                                    influence.affectSpawnablePosition(spawnablePosition)
                                }
                                spawnablePositions.add(spawnablePosition)
                                // The position BlockPos has been used in a spawnable position, editing the same one
                                // will cause entities to spawn at the wrong location (buried in walls, usually).
                                // I made it so that built-in spawnable position calculators explicitly take a copy of the
                                // BlockPos but it'd still be exposed in custom spawnable positions so fixing it here too so
                                // custom spawnable position calculators don't have to remember to do it. - Hiroku
                                pos = BlockPos.MutableBlockPos(1, 2, 3)
                                input.position = pos
                            }
                        }
                    }
                    z++
                }
                y++
                z = zone.baseZ
            }
            x++
            y = zone.baseY
            z = zone.baseZ
        }

        return spawnablePositions
    }
}