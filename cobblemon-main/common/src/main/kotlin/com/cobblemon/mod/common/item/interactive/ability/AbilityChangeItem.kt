/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item.interactive.ability

import com.cobblemon.mod.common.api.abilities.PotentialAbility
import com.cobblemon.mod.common.api.interaction.PokemonEntityInteraction
import com.cobblemon.mod.common.api.item.ability.AbilityChanger
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.CobblemonItem
import com.cobblemon.mod.common.util.asTranslated
import com.cobblemon.mod.common.util.lang
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Rarity

open class AbilityChangeItem<T : PotentialAbility>(
    val changer: AbilityChanger<T>
) : CobblemonItem(Properties().apply {
    when(changer) {
        AbilityChanger.HIDDEN_ABILITY -> rarity(Rarity.EPIC)
        AbilityChanger.COMMON_ABILITY -> rarity(Rarity.RARE)
    }
}), PokemonEntityInteraction {

    override val accepted: Set<PokemonEntityInteraction.Ownership> = setOf(PokemonEntityInteraction.Ownership.OWNER)

    override fun processInteraction(player: ServerPlayer, entity: PokemonEntity, stack: ItemStack): Boolean {
        if (this.changer.performChange(entity.pokemon)) {
            stack.consume(1, player)
            val feedback = lang(
                "ability_changer.changed",
                entity.pokemon.getDisplayName(),
                entity.pokemon.ability.displayName.asTranslated()
            )
            player.sendSystemMessage(feedback)
            return true
        }
        return false
    }

}