/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.pokemon

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.moLangFunctionMap
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.trade.TradeParticipant
import com.cobblemon.mod.common.util.getPlayer

/**
 * Event fired when a trade is initiated or completed.
 *
 * @author MeAlam
 * @since August 23th, 2025
 */
interface TradeEvent {

    /**
     * The first [TradeParticipant].
     */
    val tradeParticipant1: TradeParticipant

    /**
     * The [Pokemon] being traded by the first participant.
     */
    val tradeParticipant1Pokemon: Pokemon

    /**
     * The second [TradeParticipant].
     */
    val tradeParticipant2: TradeParticipant

    /**
     * The [Pokemon] being traded by the second participant.
     */
    val tradeParticipant2Pokemon: Pokemon

    /**
     * Event fired when a trade is about to happen. Cancelling this event prevents the trade from occurring.
     *
     * @author MeAlam
     * @since August 23th, 2025
     */
    data class Pre(
        override val tradeParticipant1: TradeParticipant,
        override val tradeParticipant1Pokemon: Pokemon,
        override val tradeParticipant2: TradeParticipant,
        override val tradeParticipant2Pokemon: Pokemon
    ) : TradeEvent, Cancelable() {
        val context = mutableMapOf(
            "trade_participant_1" to (tradeParticipant1.uuid.getPlayer()?.asMoLangValue() ?: DoubleValue.ZERO),
            "trade_participant_2" to (tradeParticipant2.uuid.getPlayer()?.asMoLangValue() ?: DoubleValue.ZERO),
            "trade_participant_1_pokemon" to tradeParticipant1Pokemon.struct,
            "trade_participant_2_pokemon" to tradeParticipant2Pokemon.struct
        )

        val functions = moLangFunctionMap(
            cancelFunc
        )
    }

    /**
     * Event fired after a trade has occurred.
     *
     * @author MeAlam
     * @since August 23th, 2025
     */
    data class Post(
        override val tradeParticipant1: TradeParticipant,
        override val tradeParticipant1Pokemon: Pokemon,
        override val tradeParticipant2: TradeParticipant,
        override val tradeParticipant2Pokemon: Pokemon
    ) : TradeEvent {
        val context = mutableMapOf(
            "trade_participant_1" to (tradeParticipant1.uuid.getPlayer()?.asMoLangValue() ?: DoubleValue.ZERO),
            "trade_participant_2" to (tradeParticipant2.uuid.getPlayer()?.asMoLangValue() ?: DoubleValue.ZERO),
            "trade_participant_1_pokemon" to tradeParticipant1Pokemon.struct,
            "trade_participant_2_pokemon" to tradeParticipant2Pokemon.struct
        )
    }
}
