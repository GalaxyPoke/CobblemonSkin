/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

fun <A, B> MutableMap<A, B>.removeIf(predicate: (Map.Entry<A, B>) -> Boolean) {
    val toRemove = mutableListOf<A>()
    for (entry in this) {
        if (predicate(entry)) {
            toRemove.add(entry.key)
        }
    }
    for (key in toRemove) {
        this.remove(key)
    }
}