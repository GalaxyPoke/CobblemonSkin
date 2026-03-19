/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.campfirepot

import com.cobblemon.mod.common.block.entity.CampfireBlockEntity
import net.minecraft.core.NonNullList
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.TransientCraftingContainer
import net.minecraft.world.item.ItemStack

class CookingPotContainer : TransientCraftingContainer {
    val menu : CookingPotMenu?
    val items : NonNullList<ItemStack>

    constructor(menu : CookingPotMenu, width: Int, height: Int) : super(menu, width, height) {
        this.menu = menu
        this.items = NonNullList.withSize(CampfireBlockEntity.Companion.ITEMS_SIZE, ItemStack.EMPTY)
    }

    override fun getContainerSize() = this.items.size
    override fun isEmpty(): Boolean {
        for (itemStack in this.items) {
            if (!itemStack.isEmpty) {
                return false
            }
        }
        return true
    }

    override fun getItem(slot: Int) = if (slot >= this.containerSize) {
        ItemStack.EMPTY
    } else {
        this.items[slot]
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        this.items[slot] = stack
        this.menu?.slotsChanged(this)
    }

    override fun removeItemNoUpdate(slot: Int) = ContainerHelper.takeItem(this.items, slot)
    override fun removeItem(slot: Int, amount: Int): ItemStack {
        val itemStack = ContainerHelper.removeItem(this.items, slot, amount)
        if (!itemStack.isEmpty) {
            this.menu?.slotsChanged(this)
        }
        return itemStack
    }

    override fun setChanged() {}
    override fun stillValid(player: Player) = true

    override fun clearContent() {
        for (index in 0 until this.items.size) {
            this.items[index] = ItemStack.EMPTY
        }
    }

    override fun getHeight() = CampfireBlockEntity.Companion.CRAFTING_GRID_WIDTH
    override fun getWidth() = CampfireBlockEntity.Companion.CRAFTING_GRID_WIDTH
    override fun getItems() = this.items.toList()
    override fun fillStackedContents(contents: StackedContents) {
        for (itemStack in this.items) {
            contents.accountSimpleStack(itemStack)
        }
    }
}