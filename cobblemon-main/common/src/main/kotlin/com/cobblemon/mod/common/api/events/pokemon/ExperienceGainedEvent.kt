/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon

import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.moLangFunctionMap
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.pokemon.experience.*
import com.cobblemon.mod.common.pokemon.Pokemon

/**
 * Event fired when experience will be gained. 
 *
 * @author Hiroku
 * @since August 5th, 2022
 */
interface ExperienceGainedEvent {
    /**
     * The [Pokemon] gaining experience.
     */
    val pokemon: Pokemon
    /**
     * The source of the experience being gained.
     */
    val source: ExperienceSource
    /**
     * The amount of experience being gained.
     */
    var experience: Int

    /**
     * Event fired when experience is about to be gained. Cancelling this event prevents
     * any experience being added, and the amount of experience can be changed from [experience].
     *
     * @author Hiroku/MeAlam
     * @since August 5th, 2022
     */
    data class Pre(
        override val pokemon: Pokemon,
        override val source: ExperienceSource,
        override var experience: Int
    ) : ExperienceGainedEvent, Cancelable() {
        val context = mutableMapOf(
            "pokemon" to pokemon.struct,
            "source" to StringValue(
                when (source) {
                    is BattleExperienceSource -> "battle"
                    is CandyExperienceSource -> "interaction"
                    is CommandExperienceSource -> "command"
                    is SidemodExperienceSource -> source.sidemodId
                    else -> "unknown"
                }
            ),
            "experience" to DoubleValue(experience.toDouble())
        )
        val functions = moLangFunctionMap(
            cancelFunc,
            "set_amount" to {
                experience = it.getInt(0)
                DoubleValue.ONE
            }
        )
    }

    /**
     * Event fired when experience has been gained. Information about whether it leveled up or not is
     * available.
     *
     * @author Hiroku/MeAlam
     * @since August 5th, 2022
     */
    data class Post(
        override val pokemon: Pokemon,
        override val source: ExperienceSource,
        override var experience: Int,
        val previousLevel: Int,
        val currentLevel: Int,
        val learnedMoves: MutableSet<MoveTemplate>
    ) : ExperienceGainedEvent {
        val context = mutableMapOf(
            "pokemon" to pokemon.struct,
            "source" to StringValue(
                when (source) {
                    is BattleExperienceSource -> "battle"
                    is CandyExperienceSource -> "interaction"
                    is CommandExperienceSource -> "command"
                    is SidemodExperienceSource -> source.sidemodId
                    else -> "unknown"
                }
            ),
            "experience" to DoubleValue(experience.toDouble()),
            "previous_level" to DoubleValue(previousLevel.toDouble()),
            "current_level" to DoubleValue(currentLevel.toDouble())
        )
    }
}