/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import java.util.Optional
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus

/**
 * Checks if a memory is registered in the brain and if it is then returns its current value.
 */
fun <T, E : LivingEntity> Brain<E>.getMemorySafely(memoryType: MemoryModuleType<T>): Optional<T> {
    val hasMemoryRegistered = checkMemory(memoryType, MemoryStatus.REGISTERED)
    return if (hasMemoryRegistered) {
        getMemory<T>(memoryType)
    } else {
        // If the memory is not registered, we return an empty Optional
        Optional.empty<T>() as Optional<T> // Why on earth is this necessary
    }
}