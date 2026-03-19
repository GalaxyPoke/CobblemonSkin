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
 * A transform that transforms the emitted values from one value to another using the given mapping function.
 *
 * @author Hiroku
 * @since November 26th, 2021
 */
class MapTransform<I, O>(private val mapping: (I) -> O) : Transform<I, O> {
    override fun invoke(input: I): O {
        return mapping(input)
    }
}