/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.IntTag
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

open class TintBlockEntity(type: BlockEntityType<*>?, blockPos: BlockPos, blockState: BlockState) : BlockEntity(type, blockPos, blockState) {
    companion object {
        const val TINT = "tint"
    }

    var tint: Int? = null

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tint?.let { tag.put(TINT, IntTag.valueOf(it)) }
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        if (tag.contains(TINT)) tint = tag.getInt(TINT)
    }

    override fun getUpdateTag(registryLookup: HolderLookup.Provider): CompoundTag {
        return this.saveWithoutMetadata(registryLookup)
    }

    fun getTint() = tint ?: 0xFFFFFF

    fun setTint(tintValue: Int) {
        level?.let {
            tint = tintValue
            setChanged()
            it.blockEntityChanged(blockPos)
            it.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_ALL)
        }
    }
}
