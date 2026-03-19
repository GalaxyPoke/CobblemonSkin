/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.interpreter

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext

/**
 * Maintains buckets of [BattleContext] during a battle.
 *
 * @author Segfault Guy
 * @since April 10th, 2023
 */
@Deprecated(message =
    "This was designed before Doubles and Multi battle formats were introduce and has not been maintained since." +
    "Accurate context for formats other than Singles is not guaranteed. This will be replaced in the near future.")
class ContextManager {

    /** [BattleContext.Type] buckets containing [BattleContexts] of that type. */
    val buckets = hashMapOf<BattleContext.Type, MutableCollection<BattleContext>>()

    /** Adds a [BattleContext] to its corresponding [BattleContext.Type] bucket. */
    fun add(vararg contexts: BattleContext) {
        contexts.forEach { context ->
            if (context.type.exclusive) {
                val bucket = this.buckets.getOrPut(context.type) { mutableListOf() }
                bucket.clear()
                bucket.add(context)
            }
            else {
                this.buckets.getOrPut(context.type) { mutableListOf() }.add(context)
            }
        }
    }

    /** Adds a [BattleContext] to its corresponding [BattleContext.Type] bucket, if the bucket does not have a context
     * of the same id. */
    fun addUnique(context: BattleContext) {
        if (this.buckets[context.type]?.find { it.id == context.id } == null) {
            add(context)
        }
    }

    /** Removes all [BattleContext]s that have IDs matching [contextID] from the [bucketType] bucket. */
    fun remove(contextID: String, bucketType: BattleContext.Type) {
        if (bucketType.exclusive) {
            this.buckets[bucketType]?.clear()
        }
        else {
            this.buckets[bucketType]?.removeIf{it.id == contextID}
        }
    }

    /** Clears all [BattleContext]s belonging to the [bucketTypes] buckets. */
    fun clear(vararg bucketTypes: BattleContext.Type) {
        bucketTypes.forEach { bucketType ->
            this.buckets[bucketType]?.clear()
        }
    }

    /** Swaps all the [BattleContext]s belonging to the [bucketTypes] buckets with the contexts of the
     * [with] manager's respective buckets. */
    fun swap(with: ContextManager, vararg bucketTypes: BattleContext.Type) {
        bucketTypes.forEach { bucketType ->
            val oldContexts = this.buckets[bucketType]?.toMutableList()
            val newContexts = with.buckets[bucketType]?.toMutableList()
            this.clear(bucketType)
            with.clear(bucketType)
            oldContexts?.let { with.add(*it.toTypedArray()) }
            newContexts?.let { this.add(*it.toTypedArray()) }
        }
    }

    /** Copies all the [BattleContext]s belonging to the [bucketTypes] buckets of the [with] manager's respective buckets. */
    fun copy(with: ContextManager, vararg bucketTypes: BattleContext.Type) {
        bucketTypes.forEach { bucketType ->
            val newContexts = with.buckets[bucketType]?.toMutableList()
            this.clear(bucketType)
            newContexts?.let { this.add(*it.toTypedArray()) }
        }
    }

    /** Gets all [BattleContext]s belonging to the [bucketType] bucket. */
    fun get(bucketType: BattleContext.Type) : Collection<BattleContext>? {
        return buckets[bucketType]
    }

    companion object {

        /**
         * Extracts a [BattleContext] from multiple context buckets.
         *
         * @return The most recently added [BattleContext] with an ID matching [contextID].
         * */
        fun scoop(contextID: String, vararg contextBuckets: Collection<BattleContext>?): BattleContext? {
            contextBuckets.filterNotNull().forEach { bucket ->
                bucket.findLast { it.id == contextID }?.let { return it }
            }
            return null
        }
    }
}