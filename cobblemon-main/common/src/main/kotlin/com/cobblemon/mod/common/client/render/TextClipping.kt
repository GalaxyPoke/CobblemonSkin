/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render

/**
 * A utility class for limiting the number of characters that can be rendered by whatever draw operation
 * is about to occur.
 *
 * Usage: Run [doWithMaxCharacters] with the maximum number of characters that will be allowed to render,
 * or -1 for unlimited, and a mixin will ensure that a string renderer will only visit that many characters
 * and cancel all those after.
 *
 * @author Hiroku
 * @since April 19th, 2025
 */
object TextClipping {
    private var acceptableRenderCount = -1
    private var renderedCount = 0

    @JvmStatic
    fun doWithMaxCharacters(maxCharacters: Int, action: Runnable) {
        acceptableRenderCount = maxCharacters
        action.run()
        acceptableRenderCount = -1
        renderedCount = 0
    }

    @JvmStatic
    fun canDrawAnotherCharacter(): Boolean {
        if (acceptableRenderCount == -1) {
            return true
        }

        if (renderedCount >= acceptableRenderCount) {
            return false
        }

        renderedCount++
        return true
    }
}