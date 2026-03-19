/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.effect

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.client.net.effect.PokeSnackBlockParticlesHandler
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Handled by [PokeSnackBlockParticlesHandler].
 */
class PokeSnackBlockParticlesPacket(
    val blockPos: BlockPos,
    val entityPos: BlockPos?
) : NetworkPacket<PokeSnackBlockParticlesPacket> {
    override val id = ID

    companion object {
        val ID = cobblemonResource("poke_snack_block_particles")
        fun decode(buffer: RegistryFriendlyByteBuf): PokeSnackBlockParticlesPacket =
            PokeSnackBlockParticlesPacket(
                buffer.readBlockPos(),
                buffer.readNullable { buffer.readBlockPos() }
            )
    }

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeBlockPos(blockPos)
        buffer.writeNullable(entityPos) { _, v -> buffer.writeBlockPos(v) }
    }
}
