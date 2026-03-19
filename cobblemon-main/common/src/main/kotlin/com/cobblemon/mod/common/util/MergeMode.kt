/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

/**
 * A way of combining two collections of items together.
 *
 * @author Hiroku
 * @since July 8th, 2022
 */
enum class MergeMode : Merger {
    /** Replaces the base list with the other list if the other list is non-null. */
    REPLACE {
        override fun <T> merge(base: MutableCollection<T>?, other: MutableCollection<T>?): MutableCollection<T>? {
            return other?.toMutableList() ?: base
        }

        override fun <T> mergeSingle(base: T?, other: T?): T? {
            return other ?: base
        }
    },
    /** Inserts the other list's contents into the base list, creating a new base list if the current is blank.*/
    INSERT {
        override fun <T> merge(base: MutableCollection<T>?, other: MutableCollection<T>?): MutableCollection<T>? {
            return if (other == null) {
                base
            } else {
                val list = base ?: mutableListOf()
                list.addAll(other)
                list
            }
        }

        override fun <T> mergeSingle(base: T?, other: T?): T? {
            return KEEP.mergeSingle(base, other)
        }
    },
    KEEP {
        override fun <T> merge(base: MutableCollection<T>?, other: MutableCollection<T>?): MutableCollection<T>? {
            return base?.toMutableList() ?: other
        }

        override fun <T> mergeSingle(base: T?, other: T?): T? {
            return base ?: other
        }
    }
}

/**
 * Interface representing the merging function used for [MergeMode]s. This is really just some Kotlin trickery to get
 * generic type functionality inside an enum.
 *
 * @author Hiroku
 * @since July 8th, 2022
 */
interface Merger {
    fun <T> merge(base: MutableCollection<T>?, other: MutableCollection<T>?): MutableCollection<T>?
    fun <T> mergeSingle(base: T?, other: T?): T?
}