/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.item.battle.BagItem
import com.cobblemon.mod.common.item.battle.SimpleBagItemLike
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class MoomooMilk(properties: Properties) : CobblemonItem(properties), SimpleBagItemLike {
    override val bagItem = object : BagItem {
        override val itemName = "item.cobblemon.moomoo_milk"
        override val returnItem = Items.GLASS_BOTTLE

        override fun canUse(stack: ItemStack, battle: PokemonBattle, target: BattlePokemon): Boolean {
            return target.health > 0
        }

        override fun getShowdownInput(actor: BattleActor, battlePokemon: BattlePokemon, data: String?): String {
            return "clear_boost"
        }
    }
}
