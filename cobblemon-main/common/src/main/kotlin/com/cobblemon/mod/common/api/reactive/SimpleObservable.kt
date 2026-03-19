/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.reactive

import com.cobblemon.mod.common.api.PrioritizedList
import com.cobblemon.mod.common.api.Priority

/**
 * A straightforward implementation of [Observable] that can emit values but holds no state.
 *
 * @author Hiroku
 * @since November 26th, 2021
 */
open class SimpleObservable<T> : Observable<T> {
    protected val subscriptions = PrioritizedList<ObservableSubscription<T>>()
    override fun subscribe(priority: Priority, handler: (T) -> Unit): ObservableSubscription<T> {
        val subscription = ObservableSubscription(this, handler)
        subscriptions.add(priority, subscription)
        return subscription
    }

    override fun unsubscribe(subscription: ObservableSubscription<T>) {
        subscriptions.remove(subscription)
    }

    open fun emit(vararg values: T) {
        if (subscriptions.isEmpty()) {
            return
        }
        values.forEach { value ->
            // One or more of these subscriptions might be removed during emission, snapshot handles this.
            val subscriptionsSnapshot = subscriptions.toList()
            subscriptionsSnapshot.forEach { subscription -> subscription.handle(value) }
        }
    }
}