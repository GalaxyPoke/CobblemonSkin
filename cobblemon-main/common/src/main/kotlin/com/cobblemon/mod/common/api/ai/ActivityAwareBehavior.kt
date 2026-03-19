/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.schedule.Activity

/**
 * A [Behavior] subclass that represents tasks that check that the entity is still in one
 * of the activities that this task is part of. If the entity is no longer doing any of those
 * activities, the task will be interrupted and you can handle any cleanup in [onInterrupted].
 *
 * @author Hiroku
 * @since October 17th, 2025
 */
abstract class ActivityAwareBehavior<E : LivingEntity> : Behavior<E> {
    constructor(entryCondition: Map<MemoryModuleType<*>, MemoryStatus>) : super(entryCondition)
    constructor(entryCondition: Map<MemoryModuleType<*>, MemoryStatus>, duration: Int) : super(entryCondition, duration)
    constructor(entryCondition: Map<MemoryModuleType<*>, MemoryStatus>, minDuration: Int, maxDuration: Int) : super(entryCondition, minDuration, maxDuration)

    val activities: MutableSet<Activity> = mutableSetOf()

    fun isStillDoingActivity(entity: E): Boolean {
        return activities.any(entity.brain::isActive)
    }

    override fun canStillUse(level: ServerLevel, entity: E, gameTime: Long) = isStillDoingActivity(entity)

    open fun onInterrupted(level: ServerLevel, entity: E, gameTime: Long) {
        // No-op
    }

    override fun stop(level: ServerLevel, entity: E, gameTime: Long) {
        super.stop(level, entity, gameTime)
        if (!isStillDoingActivity(entity)) {
            onInterrupted(level, entity, gameTime)
        }
    }
}