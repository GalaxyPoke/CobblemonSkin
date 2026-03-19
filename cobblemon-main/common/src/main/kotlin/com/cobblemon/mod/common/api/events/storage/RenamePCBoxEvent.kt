/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.storage

import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.storage.pc.PCBox
import net.minecraft.server.level.ServerPlayer

/**
 * Events fired whenever a player renames one of their PC boxes.
 * Has a cancelable [Pre] event and a [Post] which gets fired after the change.
 *
 * @author JustAHuman-xD
 * @since February 14th, 2025
 */
interface RenamePCBoxEvent {

    /**
     * The [ServerPlayer] who is changing their wallpaper
     */
    val player: ServerPlayer

    /**
     * The [PCBox] whose wallpaper is being changed
     */
    val box: PCBox

    /**
     * The new box name being used. This can be modified in the [Pre] event.
     */
    val name: String

    class Pre(
        override val player: ServerPlayer,
        override val box: PCBox,
        override var name: String
    ) : RenamePCBoxEvent, Cancelable()

    class Post(
        override val player: ServerPlayer,
        override val box: PCBox,
        override val name: String
    ) : RenamePCBoxEvent
}