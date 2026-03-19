/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.block

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.net.serverhandling.block.AdjustBlockEntityViewerCountHandler
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Handled by [AdjustBlockEntityViewerCountHandler].
 */
class AdjustBlockEntityViewerCountPacket(val blockPos: BlockPos, val increment: Boolean) : NetworkPacket<AdjustBlockEntityViewerCountPacket> {
    override val id = ID
    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBlockPos(blockPos)
        buffer.writeBoolean(increment)
    }

    companion object {
        val ID = cobblemonResource("adjust_block_entity_viewer_count")
        fun decode(buffer: RegistryFriendlyByteBuf): AdjustBlockEntityViewerCountPacket = AdjustBlockEntityViewerCountPacket(buffer.readBlockPos(), buffer.readBoolean())
    }
}