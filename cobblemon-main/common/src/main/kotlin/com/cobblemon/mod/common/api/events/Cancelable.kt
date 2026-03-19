/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events

import com.bedrockk.molang.runtime.MoParams
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue

/**
 * Something that can be canceled. This is a highly complex class and should only be read by professional engineers.
 *
 * @author Hiroku
 * @since February 18th, 2022
 */
abstract class Cancelable {
    var isCanceled: Boolean = false
        private set

    fun cancel() {
        isCanceled = true
    }

    val cancelFunc: Pair<String, (MoParams) -> MoValue> = "cancel" to {
        cancel()
        DoubleValue.ONE
    }
}