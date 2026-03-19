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
 * A single component of an action effect. When played it must return a completable future that completes
 * when the action this keyframe represents is finished. [interrupt] is provided for when something successfully
 * interrupts the effect so that if any cancellation logic is necessary, it can be provided.
 *
 * @author Hiroku
 * @since October 26th, 2023
 */
interface ActionEffectKeyframe {
    companion object {
        val types = mutableMapOf<String, Class<out ActionEffectKeyframe>>()

        inline fun <reified T : ActionEffectKeyframe> register(type: String) {
            types[type] = T::class.java
        }
    }

    fun play(context: ActionEffectContext): CompletableFuture<Unit>
    fun interrupt(context: ActionEffectContext) {}
    fun skip(): CompletableFuture<Unit> = CompletableFuture.completedFuture(Unit)
}
