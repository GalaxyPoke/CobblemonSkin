/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battles.interpreter

import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.battles.interpreter.CobblemonEffect

/**
 * A representation of an effect received from Showdown inside a [BattleMessage].
 *
 * @author Licious
 * @since December 31st, 2022
 */
interface Effect {

    /**
     * The literal ID of this [Effect].
     */
    val id: String

    /**
     * The [Effect.Type] of this [Effect].
     */
    val type: Type

    /**
     * The raw literal data received when parsing this [Effect].
     */
    val rawData: String

    val typelessData: String
        get() = rawData.substringAfter(type.prefix).trim()

    /**
     * Represents the type of the [Effect].
     *
     * @property prefix The literal prefix used to identify the type by the Showdown simulator protocol.
     *
     * @author Licious
     * @since December 31st, 2022
     */
    enum class Type(val prefix: String) {

        ABILITY("ability:"),
        ITEM("item:"),
        MOVE("move:"),
        BAGITEM("bagitem:"),
        PURE(""),

    }

    companion object {

        /**
         * Creates an [Effect].
         *
         * @param id The ID of the effect, this may be validated
         * @param type The [Effect.Type].
         * @param rawData The received string in the battle message.
         * @return An [Effect] containing the given data of the given [type].
         */
        private fun of(id: String, type: Type, rawData: String): Effect = CobblemonEffect(id, type, rawData)

        /**
         * Creates an [Effect] with type [Effect.Type.ABILITY].
         *
         * @param id The ID of the effect, meant to be present in [Abilities].
         * @param rawData The received string in the battle message.
         * @return An [Effect] containing the given data of [Effect.Type.ABILITY].
         *
         * @throws IllegalArgumentException If the [id] cannot be found in the [Abilities] registry.
         */
        fun ability(id: String, rawData: String): Effect {
            if (Abilities.get(id) == null) {
                throw IllegalArgumentException("Cannot instance ability effect with ID $id")
            }
            return of(id, Type.ABILITY, rawData)
        }

        /**
         * Creates an [Effect] with type [Effect.Type.ITEM].
         *
         * @param id The ID of the effect. This should be trusted as Showdown keeps the held items on their end.
         * @param rawData The received string in the battle message.
         * @return An [Effect] containing the given data of [Effect.Type.ITEM].
         */
        fun item(id: String, rawData: String): Effect = of(id, Type.ITEM, rawData)

        /**
         * Creates an [Effect] with type [Effect.Type.MOVE].
         *
         * @param id The ID of the effect, meant to be present in [Moves].
         * @param rawData The received string in the battle message.
         * @return An [Effect] containing the given data of [Effect.Type.MOVE].
         *
         * @throws IllegalArgumentException If the [id] cannot be found in the [Moves] registry.
         */
        fun move(id: String, rawData: String): Effect {
            if (Moves.getByName(id) == null) {
                throw IllegalArgumentException("Cannot instance move effect with ID $id")
            }
            return of(id, Type.MOVE, rawData)
        }

        /**
         * Creates a "pure" [Effect].
         * This is the representation of a purely literal effect by Showdown.
         *
         * @param id The ID of the effect.
         * @param rawData The received string in the battle message.
         * @return An [Effect] containing the given data of [Effect.Type.PURE].
         */
        fun pure(id: String, rawData: String): Effect = of(id, Type.PURE, rawData)

        /**
         * Parses an [Effect] from the given [rawData].
         *
         * @param rawData The raw data expecting to create an [Effect].
         * @return The parsed [Effect] or null if the raw data is blank or the parsed effect throws an [IllegalArgumentException].
         *
         * @throws IllegalArgumentException if the parsed type doesn't allow the resulting ID.
         */
        fun parse(rawData: String): Effect? {
            if (rawData.isBlank()) {
                return null
            }
            return try {
                when {
                    rawData.startsWith(Type.ABILITY.prefix) -> ability(rawData.lowercase().substringAfter(Type.ABILITY.prefix).replace(ShowdownIdentifiable.REGEX, ""), rawData)
                    rawData.startsWith(Type.ITEM.prefix) -> item(rawData.lowercase().substringAfter(Type.ITEM.prefix).replace(ShowdownIdentifiable.REGEX, ""), rawData)
                    rawData.startsWith(Type.MOVE.prefix) -> move(rawData.lowercase().substringAfter(Type.MOVE.prefix).replace(ShowdownIdentifiable.REGEX, ""), rawData)
                    else -> pure(rawData.lowercase().replace(ShowdownIdentifiable.REGEX, ""), rawData)
                }
            } catch (_: Exception) {
                null
            }
        }

    }

}