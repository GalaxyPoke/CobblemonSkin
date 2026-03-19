/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokemon

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.entity.Despawner
import net.minecraft.world.entity.Entity
import com.cobblemon.mod.common.config.CobblemonConfig

/**
 * The aging despawner applies strictly to mobs that can age. Its logic is relatively simple: the closer to
 * the [CobblemonConfig.despawnerNearDistance] that the entity is to a player, the older the entity must be to be despawned. At
 * [CobblemonConfig.despawnerNearDistance], an entity must be [CobblemonConfig.despawnerMaxAgeTicks] to despawn. At [CobblemonConfig.despawnerFarDistance], an entity must be [CobblemonConfig.despawnerMinAgeTicks]
 * to despawn. The required age moves gradually for all distances between near and far.
 *
 * @author Hiroku
 * @since March 19th, 2022
 */
class CobblemonAgingDespawner<T : Entity>(
    val getAgeTicks: (T) -> Int
) : Despawner<T> {

    private val nearToFar = Cobblemon.config.despawnerFarDistance - Cobblemon.config.despawnerNearDistance
    private val youngToOld = Cobblemon.config.despawnerMaxAgeTicks - Cobblemon.config.despawnerMinAgeTicks

    override fun beginTracking(entity: T) {}

    override fun shouldDespawn(entity: T): Boolean {
        val age = getAgeTicks(entity)
        if (age < Cobblemon.config.despawnerMinAgeTicks || (entity is PokemonEntity && entity.isBusy) || entity.isPassenger()) {
            return false
        }

        // TODO an AFK check at some point, don't count the AFK ones.
        val closestDistance = entity.level().players().minOfOrNull { it.distanceTo(entity) } ?: Float.MAX_VALUE
        return when {
            closestDistance < Cobblemon.config.despawnerNearDistance -> false
            age > Cobblemon.config.despawnerMaxAgeTicks || closestDistance > Cobblemon.config.despawnerFarDistance -> true
            else -> {
                val distanceRatio = (closestDistance - Cobblemon.config.despawnerNearDistance) / nearToFar
                val maximumAge = (1 - distanceRatio) * youngToOld
                age > maximumAge
            }
        }
    }
}