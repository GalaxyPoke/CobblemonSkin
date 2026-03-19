/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.scheduling

/**
 * A data block for scheduled tasks. This is to neatly contain the data for an
 * implementation of [Schedulable].
 *
 * @author Hiroku
 * @since November 5th, 2023
 */
open class SchedulingTracker {
    private val tasks = mutableListOf<ScheduledTask>()

    fun clear() {
        tasks.clear()
    }

    fun update(deltaSeconds: Float) {
        for (task in tasks.toList()) {
            task.update(deltaSeconds)
            if (task.expired) {
                tasks.remove(task)
            }
        }
    }

    fun addTask(task: ScheduledTask): ScheduledTask {
        tasks.add(task)
        return task
    }
}