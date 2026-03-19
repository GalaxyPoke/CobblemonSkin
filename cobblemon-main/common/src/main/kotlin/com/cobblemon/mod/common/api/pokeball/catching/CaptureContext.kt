/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball.catching

data class CaptureContext(
    val numberOfShakes: Int,
    val isSuccessfulCapture: Boolean,
    val isCriticalCapture: Boolean
) {

    companion object {

        /**
         * Creates a successful capture.
         * The amount of [CaptureContext.numberOfShakes] will be 4 if [critical] is false otherwise 1.
         *
         * @param critical If the capture is a critical capture, defaults to false.
         * @return The generated [CaptureContext].
         */
        fun successful(critical: Boolean = false): CaptureContext {
            if (critical) {
                return CaptureContext(numberOfShakes = 1, isSuccessfulCapture = true, isCriticalCapture = true)
            }
            return CaptureContext(numberOfShakes = 4, isSuccessfulCapture = true, isCriticalCapture = false)
        }

    }

}