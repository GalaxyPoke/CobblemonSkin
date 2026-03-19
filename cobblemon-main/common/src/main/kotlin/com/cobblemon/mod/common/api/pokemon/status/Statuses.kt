/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.status

import com.cobblemon.mod.common.pokemon.status.PersistentStatus
import com.cobblemon.mod.common.pokemon.status.VolatileStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.BurnStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.FrozenStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.ParalysisStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.PoisonBadlyStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.PoisonStatus
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.SleepStatus
import com.cobblemon.mod.common.pokemon.status.statuses.nonpersistent.ConfuseStatus
import com.cobblemon.mod.common.pokemon.status.statuses.nonpersistent.AttractStatus
import net.minecraft.resources.ResourceLocation

/**
 * Main API point for Statuses
 * Get or register Statuses
 *
 * NOTE: May seem weird to have so many things called volatile statuses but the package is called nonpersistent.
 * Its because volatile is a reserved keyword in Java. Cant use it in a package name
 *
 * @author Deltric
 */
object Statuses {
    private val persistentStatuses = mutableListOf<Status>()
    private val volatileStatuses = mutableListOf<Status>()
    private val allStatuses = mutableListOf<Status>()

    @JvmField
    val POISON = registerStatus(PoisonStatus())
    @JvmField
    val POISON_BADLY = registerStatus(PoisonBadlyStatus())
    @JvmField
    val PARALYSIS = registerStatus(ParalysisStatus())
    @JvmField
    val SLEEP = registerStatus(SleepStatus())
    @JvmField
    val FROZEN = registerStatus(FrozenStatus())
    @JvmField
    val BURN = registerStatus(BurnStatus())
    @JvmField
    val ATTRACT = registerStatus(AttractStatus())
    @JvmField
    val CONFUSE = registerStatus(ConfuseStatus())

    @JvmStatic
    fun <T: Status> registerStatus(status: T) : T {
        if (status is PersistentStatus) {
            persistentStatuses.add(status)
        }
        else if (status is VolatileStatus) {
            volatileStatuses.add(status)
        }
        allStatuses.add(status)
        return status
    }

    @JvmStatic
    fun getStatus(name: ResourceLocation) = allStatuses.find { status -> status.name == name }
    @JvmStatic
    fun getStatus(showdownName: String) = allStatuses.find { it.showdownName == showdownName }
    @JvmStatic
    fun getPersistentStatuses() = persistentStatuses
}