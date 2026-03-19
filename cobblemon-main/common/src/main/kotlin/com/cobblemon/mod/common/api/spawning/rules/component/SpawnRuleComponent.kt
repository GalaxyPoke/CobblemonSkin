/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.rules.component

import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence

/**
 * Responsible for changing a thing about spawns. A spawn rule may have many components like this.
 *
 * @author Hiroku
 * @since September 30th, 2023
 */
interface SpawnRuleComponent : SpawningInfluence {
    companion object {
        val types = mutableMapOf<String, Class<out SpawnRuleComponent>>()

        inline fun <reified T : SpawnRuleComponent> register(type: String) {
            types[type] = T::class.java
        }
    }
}