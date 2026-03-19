/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.moves.animations.keyframes

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext
import com.cobblemon.mod.common.util.asExpressionLike
import java.util.concurrent.CompletableFuture

class ForkActionEffectKeyframe(
    val condition: ExpressionLike = "true".asExpressionLike(),
    val ifTrue: List<ActionEffectKeyframe> = listOf(),
    val ifFalse: List<ActionEffectKeyframe> = listOf()
) : ActionEffectKeyframe {
    override fun play(context: ActionEffectContext): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        /*
         * Using .toList() before running chainKeyframes because iterators can be weird about sharing,
         * .toList() creates a copy of the list and then grabs its iterator so much safer (three parachutes)
         */
        if (!condition.resolveBoolean(context.runtime)) {
            context.actionEffect.chainKeyframes(context, ifFalse.toList().iterator(), future)
        } else {
            context.actionEffect.chainKeyframes(context, ifTrue.toList().iterator(), future)
        }

        return future
    }
}