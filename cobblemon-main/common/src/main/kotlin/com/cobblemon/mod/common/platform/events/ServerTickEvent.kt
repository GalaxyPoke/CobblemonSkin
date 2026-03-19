/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.platform.events

import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import net.minecraft.server.MinecraftServer

/**
 * Event fired each time the server ticks.
 *
 * @author Licious
 * @since February 15th, 2023
 */
interface ServerTickEvent {

    /**
     * The [MinecraftServer] instance.
     */
    val server: MinecraftServer

    val context: MutableMap<String, MoValue>
        get() = mutableMapOf("server" to server.asMoLangValue())

    /**
     * Fired during the Pre tick phase.
     */
    data class Pre(override val server: MinecraftServer) : ServerTickEvent

    /**
     * Fired during the Post tick phase.
     */
    data class Post(override val server: MinecraftServer) : ServerTickEvent

}