/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.campfirepot

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.CobblemonSounds
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class CookingPotResultSlot(
    container: CraftingContainer,
    index: Int,
    x: Int,
    y: Int
) : Slot(container, index, x, y) {

    override fun onTake(player: Player, stack: ItemStack) {
        if (stack.has(CobblemonItemComponents.FLAVOUR)) {
            stack.set(CobblemonItemComponents.CRAFTED, true)
        }

        val menu = player.containerMenu
        if (menu is CookingPotMenu) {
            menu.broadcastChanges()
            player.playNotifySound(CobblemonSounds.CAMPFIRE_POT_TAKE_ITEM, SoundSource.MASTER, 1.0f, 1.0f)
        }

        super.onTake(player, stack)
    }

    override fun mayPlace(stack: ItemStack): Boolean = false
}