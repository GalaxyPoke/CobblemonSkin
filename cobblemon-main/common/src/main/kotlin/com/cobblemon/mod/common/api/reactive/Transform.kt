/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.reactive

/**
 * A transformation function that can be used in [Observable.pipe] to translate the [Observable] in some way.
 * If an input value should not have an output, using [Transform.noTransform] will throw a controlled exception
 * which prevents the [Observable] from propagating anything for that input. That function has a parameter for
 * whether the transformed [Observable] should clear all subscriptions.
 *
 * @author Hiroku
 * @since November 26th, 2021
 */
interface Transform<I, O> {
    companion object {
        private val noTransformNoTerminateThrowable = NoTransformThrowable(false)
        private val noTransformTerminateThrowable = NoTransformThrowable(true)
    }

    @Throws
    operator fun invoke(input: I): O

    @Throws
    fun noTransform(terminate: Boolean): Nothing = throw if (terminate) noTransformTerminateThrowable else noTransformNoTerminateThrowable
}