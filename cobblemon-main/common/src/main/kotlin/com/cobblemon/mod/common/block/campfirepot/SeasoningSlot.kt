/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.campfirepot

import net.minecraft.world.Container
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class SeasoningSlot(val menu: CookingPotMenu, container: Container, slot: Int, x: Int, y: Int) : Slot(container, slot, x, y) {
    override fun mayPlace(stack: ItemStack): Boolean {
        val recipe = menu.currentActiveRecipe?.value() ?: return false
        return stack.`is`(recipe.seasoningTag) && super.mayPlace(stack)
    }
}