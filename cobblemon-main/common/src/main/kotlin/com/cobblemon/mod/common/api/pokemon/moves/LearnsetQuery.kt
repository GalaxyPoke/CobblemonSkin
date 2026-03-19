/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.moves

import com.cobblemon.mod.common.api.moves.MoveTemplate

/**
 * A functional interface for a query to the learnset of a Pokémon.
 * A few default implementations to make common queries can be found in [LearnsetQuery.Companion].
 *
 * @author Licious
 * @since November 1st, 2022
 */
fun interface LearnsetQuery {

    fun canLearn(move: MoveTemplate, learnset: Learnset): Boolean

    companion object {

        val ANY = LearnsetQuery { move, learnset ->
            learnset.levelUpMoves.values.any { it.contains(move) }
                    || learnset.eggMoves.contains(move)
                    || learnset.tutorMoves.contains(move)
                    || learnset.tmMoves.contains(move)
                    || learnset.formChangeMoves.contains(move)
                    || learnset.evolutionMoves.contains(move)
                    || learnset.legacyMoves.contains(move)
                    || learnset.specialMoves.contains(move)
        }

        val LEGAL = LearnsetQuery { move, learnset ->
            learnset.levelUpMoves.values.any { it.contains(move) }
                    || learnset.eggMoves.contains(move)
                    || learnset.tutorMoves.contains(move)
                    || learnset.tmMoves.contains(move)
                    || learnset.formChangeMoves.contains(move)
                    || learnset.evolutionMoves.contains(move)
        }

        fun level(level: Int) = LearnsetQuery { move, learnset -> learnset.getLevelUpMovesUpTo(level).contains(move) }

        val ANY_LEVEL = LearnsetQuery { move, learnset -> learnset.levelUpMoves.values.any { it.contains(move) } }

        val EGG_MOVE = LearnsetQuery { move, learnset -> learnset.eggMoves.contains(move) }

        val TUTOR_MOVES = LearnsetQuery { move, learnset -> learnset.tutorMoves.contains(move) }

        val TM_MOVE = LearnsetQuery { move, learnset -> learnset.tmMoves.contains(move) }

        val FORM_CHANGE = LearnsetQuery { move, learnset -> learnset.formChangeMoves.contains(move) }

        val EVOLUTION = LearnsetQuery { move, learnset -> learnset.evolutionMoves.contains(move) }

        val LEGACY_MOVES = LearnsetQuery { move, learnset -> learnset.legacyMoves.contains(move) }

        val SPECIAL_MOVES = LearnsetQuery { move, learnset -> learnset.specialMoves.contains(move) }
    }

}