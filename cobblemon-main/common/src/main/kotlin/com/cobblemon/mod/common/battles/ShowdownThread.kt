/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.battles.runner.ShowdownService
import java.util.*
import java.util.concurrent.CountDownLatch

class ShowdownThread : Thread("Cobblemon Showdown") {

    private val latch = CountDownLatch(1)

    private val whenReady : Queue<(ShowdownService) -> Unit> = LinkedList()

    fun launch() {
        this.start()
        this.latch.await()
        for (action in whenReady) {
            action(ShowdownService.service)
        }
    }

    fun queue(action: (ShowdownService) -> Unit) {
        if (this.latch.count == 0L) {
            action(ShowdownService.service)
        } else {
            this.whenReady.add(action)
        }
    }

    override fun run() {
        LOGGER.info("Starting showdown service...")
        ShowdownService.service.openConnection()
        LOGGER.info("Showdown has been started!")
        this.latch.countDown()
    }
}