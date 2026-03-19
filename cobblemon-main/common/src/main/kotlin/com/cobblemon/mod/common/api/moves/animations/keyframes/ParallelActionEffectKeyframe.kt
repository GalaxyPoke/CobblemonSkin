/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves.animations.keyframes

import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import java.util.concurrent.CompletableFuture

class ParallelActionEffectKeyframe : ConditionalActionEffectKeyframe() {
    var keyframes: MutableList<ActionEffectKeyframe> = mutableListOf()

    override fun playWhenTrue(context: ActionEffectContext): CompletableFuture<Unit> {
        return CompletableFuture.allOf(
            *keyframes.map {
                context.currentKeyframes.add(it)
                it.play(context).thenRun { context.currentKeyframes.remove(it) }
            }.toTypedArray()
        ).thenApply {} // Map Void -> Unit at the end
    }
}