/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.effect

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.client.net.effect.SpawnSnowstormEntityParticleHandler
import com.cobblemon.mod.common.util.*
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * Packet that spawns a snowstorm particle effect on a specified entity and specified locator.
 * Can specify an optional target+locator to pass through to the spawned particle
 *
 * Handled by [SpawnSnowstormEntityParticleHandler]
 *
 * @author Hiroku
 * @since January 21st, 2024
 */
class SpawnSnowstormEntityParticlePacket(
    val effectId: ResourceLocation,
    val sourceEntityId: Int,
    val sourceLocators: List<String> = listOf("root"),
    val targetedEntityId: Int? = null,
    val targetLocators: List<String>? = null
) : NetworkPacket<SpawnSnowstormEntityParticlePacket> {
    companion object {
        val ID = cobblemonResource("spawn_snowstorm_entity_particle")

        fun decode(buffer: RegistryFriendlyByteBuf): SpawnSnowstormEntityParticlePacket {
            val effectId = buffer.readIdentifier()
            val sourceEntityId = buffer.readInt()
            val sourceLocators = buffer.readList { buffer.readString() }
            val targetedEntityId = buffer.readNullable { it.readInt() }
            val targetLocators = buffer.readNullable { buffer.readList { buffer.readString() }}
            return SpawnSnowstormEntityParticlePacket(effectId, sourceEntityId, sourceLocators, targetedEntityId, targetLocators)
        }
    }

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(effectId)
        buffer.writeInt(sourceEntityId)
        buffer.writeCollection(sourceLocators) { _, value -> buffer.writeString(value) }
        buffer.writeNullable(targetedEntityId) { _, value -> buffer.writeInt(value) }
        buffer.writeNullable(targetLocators) { _, value ->
            buffer.writeCollection(value) { _, locator ->
                buffer.writeString(locator)
            }
        }
    }
}