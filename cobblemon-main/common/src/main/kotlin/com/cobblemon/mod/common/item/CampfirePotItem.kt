/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.CobblemonBlocks
import com.cobblemon.mod.common.block.campfirepot.CampfirePotColor
import com.cobblemon.mod.common.CobblemonSounds
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.CampfireBlock
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.entity.CampfireBlockEntity

class CampfirePotItem(block: Block, val color: CampfirePotColor): BlockItem(block, Properties()) {

    override fun useOn(context: UseOnContext): InteractionResult {
        val world = context.level
        val blockPos = context.clickedPos
        val blockState = world.getBlockState(blockPos)
        val blockEntity = world.getBlockEntity(blockPos)
        val player = context.player

        if (player != null && blockState.block is CampfireBlock && blockEntity is CampfireBlockEntity) {
            if (blockState.getValue(CampfireBlock.LIT)) {
                // Remove the existing block entity and replace with the custom CampfireBlockEntity
                val facing = blockState.getValue(HorizontalDirectionalBlock.FACING)
                val itemFacing = Direction.fromYRot(player.getYHeadRot().toDouble())
                val isSoul = blockState.block.asItem().toString() == "minecraft:soul_campfire"

                // Drop items stored in campfire
                for (item in blockEntity.items) {
                    if (!item.isEmpty) {
                        val itemEntity = ItemEntity(
                            world,
                            blockPos.x + 0.5,
                            (blockPos.y + 1).toDouble(),
                            blockPos.z + 0.5, item
                        )
                        itemEntity.setDefaultPickUpDelay()
                        world.addFreshEntity(itemEntity)
                    }
                }

                blockEntity.setRemoved()

                val newBlockState = (if (isSoul) CobblemonBlocks.SOUL_CAMPFIRE else CobblemonBlocks.CAMPFIRE).defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, facing)
                    .setValue(com.cobblemon.mod.common.block.campfirepot.CampfireBlock.Companion.ITEM_DIRECTION, itemFacing.opposite)
                world.setBlockAndUpdate(blockPos, newBlockState)

                // Retrieve the new block entity and set the PotItem
                val newBlockEntity = world.getBlockEntity(blockPos)
                if (newBlockEntity is com.cobblemon.mod.common.block.entity.CampfireBlockEntity) {
                    if (newBlockEntity.getPotItem() == null || newBlockEntity.getPotItem()!!.isEmpty) {
                        newBlockEntity.setPotItem(ItemStack(this).split(1))
                        context.itemInHand.consume(1, player)
                        world.playSound(null, blockPos, CobblemonSounds.CAMPFIRE_POT_SET, SoundSource.BLOCKS, 1.0F, 1.0F)
                        return InteractionResult.SUCCESS
                    }
                }
            }
            // Don't place pot if campfire is not lit
            else if (!player.isCrouching) {
                return InteractionResult.FAIL
            }
        }

        return super.useOn(context)
    }
}