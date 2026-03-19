/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.condition

import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.util.adapters.SpawningConditionAdapter

/**
 * An appendage to [SpawningCondition] that deserializes from the root of a spawning condition
 * and is then stowed in [SpawningCondition.appendages].
 *
 * As an example, you could add an appendage which has a single field called 'weather'. The [fits]
 * implementation would check the [SpawnablePosition] using your own logic. Inside the JSON, you
 * would specify the 'weather' property as if it was a built-in field inside [SpawningCondition]. During
 * loading, it will deserialize the [SpawningCondition]'s JSON several times targeting the various
 * appendage classes, and then add those appendages to [SpawningCondition.appendages] so that when
 * the [SpawningCondition] is checked, the appendages are used as extra conditions.
 *
 * There are some conditions that must be met by all implementations of this interface. The class must
 * have a default constructor. Ideally, all the properties should be explicitly nullable.
 *
 * To register an appendage, you run [AppendageCondition.registerAppendage] and you can specify the target
 * [SpawningCondition] class that the appendage applies to or be more specific and provide a predicate that
 * takes a deserialized [SpawningCondition] and returns true if your appendage applies to this.
 *
 * The loading logic for this can be found in the [SpawningConditionAdapter].
 *
 * This solution was shamelessly stolen from Ultima Quests: https://cable-mc.com/docs/ultimaquests.pdf#subsection.8.5
 *
 * @author Hiroku
 * @since July 9th, 2022
 */
interface AppendageCondition {
    private class RegisteredAppendageCondition(
        val clazz: Class<out AppendageCondition>,
        val spawningConditionFits: (SpawningCondition<*>) -> Boolean
    )

    companion object {
        private val appendages = mutableListOf<RegisteredAppendageCondition>()
        fun registerAppendage(conditionClass: Class<out SpawningCondition<*>>, appendageClass: Class<out AppendageCondition>) {
            registerAppendage(appendageClass, conditionClass::isInstance)
        }

        fun registerAppendage(appendageClass: Class<out AppendageCondition>, spawningConditionFits: (SpawningCondition<*>) -> Boolean) {
            appendages.add(RegisteredAppendageCondition(appendageClass, spawningConditionFits))
        }

        fun getAppendages(spawningCondition: SpawningCondition<*>) = appendages.filter { it.spawningConditionFits(spawningCondition) }.map { it.clazz }
    }
    fun fits(spawnablePosition: SpawnablePosition): Boolean
}