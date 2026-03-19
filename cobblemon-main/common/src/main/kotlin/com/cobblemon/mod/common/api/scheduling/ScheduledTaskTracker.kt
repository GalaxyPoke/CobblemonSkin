/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.scheduling

/**
 * More precise than the [ServerTaskTracker], still runs on the server, and uses epoch time deltas for
 * the scheduling delta so that it is working by real time and not by ticks. This is useful for some
 * applications but is generally not what you want.
 */
object ServerRealTimeTaskTracker : Schedulable, SchedulingTracker() {
    override val schedulingTracker = this
    var lastTicked = System.currentTimeMillis()

    fun update() {
        val now = System.currentTimeMillis()
        val delta = now - lastTicked
        lastTicked = now
        update(delta / 1000F)
    }
}


object ServerTaskTracker: Schedulable, SchedulingTracker() {
    override val schedulingTracker = this
}

object ClientTaskTracker : Schedulable, SchedulingTracker() {
    override val schedulingTracker = this
}