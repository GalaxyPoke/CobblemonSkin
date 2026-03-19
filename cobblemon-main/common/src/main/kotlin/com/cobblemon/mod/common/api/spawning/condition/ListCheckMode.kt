/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

/**
 * The way a list of condition-type properties are checked.
 *
 * ALL represents the need for every element of the list to be present.
 * ANY represents the need for only a single elemt of the list to be present.
 *
 * @author Hiroku
 * @since February 7th, 2022
 */
enum class ListCheckMode {
    /** Represents the need for every element of the list to be present. */
    ALL,
    /** Represents the need for only a single elemt of the list to be present. */
    ANY
}