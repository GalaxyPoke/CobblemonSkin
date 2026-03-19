/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.reactive.pipes

import com.cobblemon.mod.common.api.reactive.Transform

/**
 * A transform that will ignore some number of initial emissions before it will continue as usual.
 *
 * @author Hiroku
 * @since November 27th, 2021
 */
class IgnoreFirstTransform<T>(var amount: Int = 1) : Transform<T, T> {
    override fun invoke(input: T): T {
        if (amount > 0) {
            amount--
            noTransform(terminate = false)
        } else {
            return input
        }
    }
}