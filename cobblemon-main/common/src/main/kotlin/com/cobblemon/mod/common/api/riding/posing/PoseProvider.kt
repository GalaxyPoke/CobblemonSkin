/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding.posing

import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourState
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity

class PoseProvider<Settings : RidingBehaviourSettings, State : RidingBehaviourState>(private val fallback: PoseType) {

    private val options = mutableListOf<PoseOption<Settings, State>>()

    fun select(settings: Settings, state: State, entity: PokemonEntity) : PoseType {
        return this.options.stream()
            .filter { it.condition(settings, state, entity) }
            .map { it.pose }
            .findFirst()
            .orElse(this.fallback)
    }

    fun with(option: PoseOption<Settings, State>) : PoseProvider<Settings, State> {
        this.options.add(option)
        return this
    }

}
