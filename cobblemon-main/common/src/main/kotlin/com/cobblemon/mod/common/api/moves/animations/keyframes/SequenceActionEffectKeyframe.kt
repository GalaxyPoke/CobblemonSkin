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

/**
 * A sequence of action effects that are dependent on a condition.
 *
 * @author Hiroku
 * @since October 29th, 2023
 */
class SequenceActionEffectKeyframe(val keyframes: List<ActionEffectKeyframe> = listOf()) : ConditionalActionEffectKeyframe() {
    override fun playWhenTrue(context: ActionEffectContext): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        context.actionEffect.chainKeyframes(context, keyframes.toList().iterator(), future)
        return future
    }
}