/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.spawner

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.detail.SpawnPool
import com.cobblemon.mod.common.util.getPlayer
import com.cobblemon.mod.common.util.nextBetween
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth.PI
import net.minecraft.util.Mth.ceil
import java.util.*
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * A spawner that works around a single player. It will do basic tracking of a player's speed
 * and project that into the future and spawn in that direction.
 *
 * @author Hiroku
 * @since February 14th, 2022
 */
open class PlayerSpawner(
    player: ServerPlayer,
    spawnPool: SpawnPool
) : BasicSpawner(
    name = player.name.string,
    spawnPool = spawnPool
) {
    val uuid: UUID = player.uuid

    fun getZoneInput(cause: SpawnCause): SpawningZoneInput? {
        val player = uuid.getPlayer() ?: return null
        val zoneDiameter = config.spawningZoneDiameter
        val zoneHeight = config.spawningZoneHeight

        val rand = Random.Default

        val center = player.position()

        val r = rand.nextBetween(config.minimumSpawningZoneDistanceFromPlayer, config.maximumSpawningZoneDistanceFromPlayer)
        val thetatemp = atan(player.deltaMovement.z / player.deltaMovement.x) + rand.nextBetween(-PI/2, PI/2 )
        val theta = if (player.deltaMovement.horizontalDistance() < 0.1) {
            rand.nextDouble() * 2 * PI
        } else if (player.deltaMovement.x < 0) {
            PI - thetatemp
        } else {
            thetatemp
        }

        val x = center.x + r * cos(theta)
        val z = center.z + r * sin(theta)

        return SpawningZoneInput(
            cause = cause,
            world = player.level() as ServerLevel,
            baseX = ceil(x - zoneDiameter / 2F),
            baseY = ceil(center.y - zoneHeight / 2F),
            baseZ = ceil(z - zoneDiameter / 2F),
            length = zoneDiameter,
            height = zoneHeight,
            width = zoneDiameter
        )
    }

    var active = true
    var ticksUntilNextSpawn = 100F
    var ticksBetweenSpawns: Float = config.ticksBetweenSpawnAttempts
    var tickTimerMultiplier = 1F

    fun tick() {
        if (!active) {
            return
        }

        ticksUntilNextSpawn -= tickTimerMultiplier
        if (ticksUntilNextSpawn <= 0) {
            val zoneInput = getZoneInput(cause = SpawnCause(spawner = this, entity = uuid.getPlayer()))
            zoneInput?.let(::runForArea)
            ticksUntilNextSpawn = ticksBetweenSpawns
        }
    }
}