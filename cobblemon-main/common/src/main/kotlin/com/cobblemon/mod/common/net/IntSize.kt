/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net

/**
 * Convenient breakdown of different sizes of integer for use in (de)serializing from byte buffers.
 *
 * @author Hiroku
 * @since November 28th, 2021
 */
enum class IntSize {
    INT,
    SHORT,
    U_SHORT,
    BYTE,
    U_BYTE
}