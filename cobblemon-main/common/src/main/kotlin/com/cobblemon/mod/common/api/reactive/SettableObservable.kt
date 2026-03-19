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
 * A type of [Observable] that wraps around a value. This means you can immediately get the current value of
 * the observable, and it's expected that external users can set the value.
 *
 * @author Hiroku
 * @since November 26th, 2021
 */
open class SettableObservable<T>(private var value: T) : Observable<T> {
    val subscriptions = PrioritizedList<ObservableSubscription<T>>()

    fun subscribeIncludingCurrent(priority: Priority = Priority.NORMAL, handler: (T) -> Unit): ObservableSubscription<T> {
        val subscription = subscribe(priority, handler)
        subscription.handle(value)
        return subscription
    }

    override fun subscribe(priority: Priority, handler: (T) -> Unit): ObservableSubscription<T> {
        val subscription = ObservableSubscription(this, handler)
        subscriptions.add(priority, subscription)
        return subscription
    }

    override fun unsubscribe(subscription: ObservableSubscription<T>) {
        subscriptions.remove(subscription)
    }

    /**
     * Emits the new value only if it is different from the current value.
     *
     * If you want to emit the new value no matter what, use [SettableObservable.emit] directly.
     */
    open fun set(newValue: T) {
        if (this.value?.equals(newValue) == true || (this.value == null && newValue == null)) {
            return
        }
        emit(newValue)
    }

    /** Emits and stores the new value. */
    open fun emit(newValue: T) {
        this.value = newValue
        subscriptions.forEach { it.handle(newValue) }
    }

    /**
     * Gets the current value of this observable. This is unique to [SettableObservable]s as [Observable]
     * does not necessarily have state.
     */
    open fun get() = this.value
}