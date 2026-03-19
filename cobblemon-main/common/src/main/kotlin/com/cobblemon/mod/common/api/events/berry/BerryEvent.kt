/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.berry

import com.cobblemon.mod.common.api.berry.Berry

/**
 * The base of all Berry related events.
 *
 * @author Licious
 * @since November 28th, 2022
 */
interface BerryEvent {

    /**
     * The [Berry] related to the event trigger
     */
    val berry: Berry

}

