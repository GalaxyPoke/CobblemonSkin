/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.platform.events

import net.minecraft.server.level.ServerPlayer

/**
 * Events for when a [player] ticks.
 *
 * @author Hiroku
 * @since February 10th, 2025
 */
interface ServerPlayerTickEvent : ServerPlayerEvent {
    /**
     * Fired when the [player] pre-ticks. For most use cases it doesn't matter if you take the pre or the post.
     */
    data class Pre(override val player: ServerPlayer) : ServerPlayerTickEvent
    /**
     * Fired when the [player] post-ticks. For most use cases it doesn't matter if you take the pre or the post.
     */
    data class Post(override val player: ServerPlayer) : ServerPlayerTickEvent
}