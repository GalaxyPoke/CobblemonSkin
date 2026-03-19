/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.net.effect

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.ClientMoLangFunctions.setupClient
import com.cobblemon.mod.common.client.particle.BedrockParticleOptionsRepository
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.entity.PosableEntity
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormEntityParticlePacket
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity

object SpawnSnowstormEntityParticleHandler : ClientNetworkPacketHandler<SpawnSnowstormEntityParticlePacket> {
    override fun handle(packet: SpawnSnowstormEntityParticlePacket, client: Minecraft) {
        val world = Minecraft.getInstance().level ?: return
        val effect = BedrockParticleOptionsRepository.getEffect(packet.effectId) ?: return
        val sourceEntity = world.getEntity(packet.sourceEntityId) as? PosableEntity ?: return
        val targetedEntity = packet.targetedEntityId?.let { world.getEntity(it) }
        sourceEntity as Entity
        val sourceState = sourceEntity.delegate as PosableState
        val targetState = (targetedEntity as? PosableEntity)?.delegate as? PosableState
        val sourceLocators = packet.sourceLocators.firstNotNullOfOrNull { sourceState.getMatchingLocators(it).takeIf { it.isNotEmpty() } } ?: return
        val targetedLocator = packet.targetLocators?.firstOrNull { targetState?.getMatchingLocators(it)?.isNotEmpty() == true }
        val rootMatrix = sourceState.locatorStates["root"]!!

        sourceLocators.forEach { locator ->
            val locatorMatrix = sourceState.locatorStates[locator]!!

            val particleMatrix = effect.emitter.space.initializeEmitterMatrix(rootMatrix, locatorMatrix)
            val particleRuntime = MoLangRuntime().setup().setupClient()
            particleRuntime.environment.query.addFunction("entity") { sourceState.runtime.environment.query }

            val storm = ParticleStorm(
                effect = effect,
                emitterSpaceMatrix = particleMatrix,
                attachedMatrix = locatorMatrix,
                world = world,
                runtime = particleRuntime,
                sourceVelocity = { sourceEntity.deltaMovement },
                sourceAlive = { !sourceEntity.isRemoved },
                sourceVisible = { !sourceEntity.isInvisible },
                targetPos =  if (targetedEntity != null) { { targetedLocator?.let { targetState?.locatorStates?.get(it) }?.getOrigin() ?: targetedEntity.position() } } else null,
                entity = sourceEntity
            )

            storm.spawn()
        }
    }
}