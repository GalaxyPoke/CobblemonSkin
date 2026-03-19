/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.helditem

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.pokemon.helditem.HeldItemManager
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon

/**
 * The Cobblemon implementation of an empty [HeldItemManager].
 * The literal IDs are the path of item identifiers under the [Cobblemon.MODID] namespace.
 *
 * @author Licious
 * @since January 6th, 2022
 */
object CobblemonEmptyHeldItemManager : BaseCobblemonHeldItemManager() {
    override fun handleStartInstruction(pokemon: BattlePokemon, battle: PokemonBattle, battleMessage: BattleMessage) {}
    override fun handleEndInstruction(pokemon: BattlePokemon, battle: PokemonBattle, battleMessage: BattleMessage) {}
}