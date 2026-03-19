/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.effect

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.block.PokeSnackBlock
import com.cobblemon.mod.common.net.messages.client.effect.PokeSnackBlockParticlesPacket
import net.minecraft.client.Minecraft
import net.minecraft.core.particles.ParticleTypes

object PokeSnackBlockParticlesHandler : ClientNetworkPacketHandler<PokeSnackBlockParticlesPacket> {
    override fun handle(packet: PokeSnackBlockParticlesPacket, client: Minecraft) {
        val level = Minecraft.getInstance().level ?: return

        val blockState = level.getBlockState(packet.blockPos)
        val block = blockState.block
        if (block is PokeSnackBlock) {
            block.spawnEatParticles(level, packet.blockPos)
        }

        val entityPos = packet.entityPos
        if (entityPos != null) {
            val random = level.random
            repeat(5) {
                level.addParticle(
                    ParticleTypes.POOF,
                    entityPos.x + random.nextDouble(),
                    entityPos.y + random.nextDouble(),
                    entityPos.z + random.nextDouble(),
                    0.0,
                    0.0,
                    0.0
                )
            }
        }
    }
}
