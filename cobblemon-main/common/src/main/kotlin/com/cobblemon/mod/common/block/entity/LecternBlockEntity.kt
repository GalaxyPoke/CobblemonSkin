/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.block.LecternBlock
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.ContainerHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class LecternBlockEntity(blockPos: BlockPos, blockState: BlockState) : ViewerCountBlockEntity(CobblemonBlockEntities.LECTERN, blockPos, blockState) {
    val inventory: NonNullList<ItemStack> = NonNullList.withSize(1, ItemStack.EMPTY)

    override fun saveAdditional(compoundTag: CompoundTag, registryLookup: HolderLookup.Provider) {
        super.saveAdditional(compoundTag, registryLookup)
        ContainerHelper.saveAllItems(compoundTag, inventory, true, registryLookup)
    }

    override fun loadAdditional(compoundTag: CompoundTag, registryLookup: HolderLookup.Provider) {
        super.loadAdditional(compoundTag, registryLookup)
        ContainerHelper.loadAllItems(compoundTag, inventory, registryLookup)
    }

    override fun getUpdateTag(registryLookup: HolderLookup.Provider): CompoundTag {
        return this.saveWithoutMetadata(registryLookup)
    }

    override fun updateBlock(level: Level, state: BlockState) {
        super.updateBlock(level, state.setValue(LecternBlock.EMIT_LIGHT, hasViewer()))
    }

    fun isEmpty(): Boolean = getItemStack().isEmpty

    fun getItemStack(): ItemStack = inventory[0]

    fun setItemStack(itemStack: ItemStack) {
        level?.let {
            inventory[0] = itemStack
            updateBlock(it, blockState)
        }
    }

    fun removeItemStack(): ItemStack {
        level?.let {
            val itemStack = ContainerHelper.removeItem(inventory, 0, 1)
            updateBlock(it, blockState)
            return itemStack
        }
        return ItemStack.EMPTY
    }
}
