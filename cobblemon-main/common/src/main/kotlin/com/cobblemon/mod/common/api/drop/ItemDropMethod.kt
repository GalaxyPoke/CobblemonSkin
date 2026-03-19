/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.drop

import com.cobblemon.mod.common.api.serialization.StringIdentifiedObjectAdapter
import com.google.gson.annotations.SerializedName

/**
 * The way an item will be dropped.
 *
 * @author Hiroku
 * @since July 25th, 2022
 */
enum class ItemDropMethod(val methodName: String) {
    /** Drops the item on the entity that is dying, if it exists. If not, drops at the position parsed into drops. */
    ON_ENTITY("on-entity"),
    /** Drops the item on the player that caused the drop, if they exist. If not, drops at the position parsed into drops. */
    ON_PLAYER("on-player"),
    /**
     * Puts the item in the player's inventory or drops it on the ground if the inventory is full. If the player that
     * caused the drop doesn't exist, it drops at the position parsed into drops.
     */
    TO_INVENTORY("to-inventory");

    companion object {
        val adapter = StringIdentifiedObjectAdapter(
            { str -> ItemDropMethod.entries.firstOrNull { it.methodName == str } },
            { value -> (value ?: ON_ENTITY).methodName },
        )
    }
}