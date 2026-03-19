/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon.stats

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

interface EvSource {

    /**
     * The [Pokemon] being affected.
     */
    val pokemon: Pokemon

    /**
     * Utility function that checks if the source is an implementation of [BattleEvSource].
     */
    fun isBattle() = this is BattleEvSource

    /**
     * Utility function that checks if the source is an implementation of [ItemEvSource].
     */
    fun isInteraction() = this is ItemEvSource

    /**
     * Utility function that checks if the source is an implementation of [SidemodEvSource].
     */
    fun isSidemod() = this is SidemodEvSource

}

/**
 * Triggered by sidemods.
 *
 * @property sidemodId The mod ID of the sidemod triggering this source.
 * @property pokemon See [EvSource.pokemon].
 */
open class SidemodEvSource(
    val sidemodId: String,
    override val pokemon: Pokemon
) : EvSource

/**
 * An Ev source fired when using Ev mutating items.
 *
 * @property player The [ServerPlayer] using the item.
 * @property stack The [ItemStack] being consumed.
 * @property pokemon See [EvSource.pokemon].
 */
open class ItemEvSource(
    val player: ServerPlayer,
    val stack: ItemStack,
    override val pokemon: Pokemon
) : EvSource

/**
 * An Ev source fired in battles.
 *
 * @property battle The associated [PokemonBattle].
 * @property facedPokemon The [BattlePokemon]s that the [pokemon] faced.
 * @property pokemon See [EvSource.pokemon], comes from the original [BattlePokemon.effectedPokemon].
 */
open class BattleEvSource(
    val battle: PokemonBattle,
    val facedPokemon: List<BattlePokemon>,
    override val pokemon: Pokemon
) : EvSource