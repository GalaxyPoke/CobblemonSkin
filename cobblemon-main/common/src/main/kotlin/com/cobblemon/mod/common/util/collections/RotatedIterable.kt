/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.collections

/**
 * An Iterable over a List<T> that provides values rotated by some value.
 *
 * e.g. a list [0, 1, 2, 3] with a startAt of 2 will be iterated over as if the list read [2, 3, 0, 1]
 */
class RotatedIterable<out T>: Iterable<T> {
    private val iter: RotatedListIterator<T>

    constructor(list: List<T>, startAt: Int) {
        this.iter = RotatedListIterator(list, startAt)
    }

    override fun iterator(): Iterator<T> = iter

    inner class RotatedListIterator<out T>: Iterator<T> {
        private val list: List<T>
        private val startAt: Int
        private var outerIdx = 0

        constructor(list: List<T>, startAt: Int) {
            this.list = list
            this.startAt = startAt
        }

        override fun hasNext(): Boolean = outerIdx < list.size

        private fun nextIndex(): Int {
            val idx = startAt + outerIdx
            outerIdx = outerIdx.inc()
            return if (idx < list.size) {
                idx
            } else {
                idx - list.size
            }
        }

        override fun next(): T = list[nextIndex()]
    }
}

