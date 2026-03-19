/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.events.berry

import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.berry.Berry
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.block.entity.BerryBlockEntity
import com.cobblemon.mod.common.util.toArrayStruct
import com.cobblemon.mod.common.util.worldRegistry
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

/**
 * An event fired when [BerryBlockEntity.harvest] is invoked.
 *
 * @property berry The [Berry] the tree is attached to.
 * @property player The [ServerPlayer] harvesting the tree.
 * @property world The [World] the tree is in.
 * @property pos The [BlockPos] of the tree.
 * @property state The [BlockState] of the tree.
 * @property blockEntity The backing [BerryBlockEntity]-
 * @property drops A collection of [ItemStack]s produced by this harvest.
 *
 * @author Licious
 * @since November 28th, 2022
 */
data class BerryHarvestEvent(
    override val berry: Berry,
    val player: ServerPlayer,
    val world: Level,
    val pos: BlockPos,
    val state: BlockState,
    val blockEntity: BerryBlockEntity,
    val drops: MutableList<ItemStack>
) : BerryEvent {
    val context = mutableMapOf(
        "player" to player.asMoLangValue(),
        "berry" to StringValue(berry.identifier.toString()),
        "world" to world.worldRegistry.wrapAsHolder(world).asMoLangValue(Registries.DIMENSION),
        "pos" to pos.toArrayStruct()
    )
}