/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.reactive

class ObservableSubscription<T>(
    private val observable: Observable<T>,
    private val handler: (T) -> Unit
) {
    var alive = true
    fun handle(value: T) = handler(value)
    fun unsubscribe() {
        observable.unsubscribe(this)
        alive = false
    }
}