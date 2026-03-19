/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.reactive

import com.cobblemon.mod.common.api.Priority

/**
 * An [SimpleObservable] implementation created by piping a root [Observable]. It will try to emit values
 * whenever the root [Observable] emits a value by running the emitted value through the given [Transform].
 * A [TransformObservable] must be subscribed to before any subscriptions are made on the root [Observable].
 *
 * This class handles [NoTransformThrowable] from the transform and will not propagate a value if that occurs.
 * If [NoTransformThrowable.terminate] is true, all subscriptions to this [Observable] will be removed, including
 * this subscription to the root [Observable].
 *
 * @author Hiroku
 * @since November 26th, 2021
 */
class TransformObservable<I, O>(
    private val observable: Observable<I>,
    private val transform: Transform<I, O>
) : SimpleObservable<O>() {
    var rootSubscription: ObservableSubscription<I>? = null

    override fun subscribe(priority: Priority, handler: (O) -> Unit): ObservableSubscription<O> {
        if (rootSubscription == null) {
            rootSubscription = observable.subscribe(priority) { parentHandler(it) }
        }

        return super.subscribe(priority, handler)
    }

    fun terminate() {
        rootSubscription?.let { observable.unsubscribe(it) }
    }

    fun parentHandler(input: I) {
        try {
            emit(transform(input))
        } catch (throwable: NoTransformThrowable) {
            if (throwable.terminate) {
                terminate()
            }
        }
    }
}