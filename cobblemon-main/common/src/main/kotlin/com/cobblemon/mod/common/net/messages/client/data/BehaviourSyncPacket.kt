/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.CobblemonBehaviours
import com.cobblemon.mod.common.api.ai.CobblemonBehaviour
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readText
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeText
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class BehaviourSyncPacket(entries: Map<ResourceLocation, CobblemonBehaviour>) : DataRegistrySyncPacket<Map.Entry<ResourceLocation, CobblemonBehaviour>, BehaviourSyncPacket>(entries.entries.toList()) {
    companion object {
        val ID = cobblemonResource("behaviour_sync")
        fun decode(buffer: RegistryFriendlyByteBuf): BehaviourSyncPacket = BehaviourSyncPacket(emptyMap()).apply { decodeBuffer(buffer) }
    }

    override val id = ID
    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): Map.Entry<ResourceLocation, CobblemonBehaviour>? {
        val identifier = buffer.readIdentifier()
        val name = buffer.readText()
        val description = buffer.readText()
        val entityType = buffer.readNullable { buffer.readIdentifier() }
        val behaviour = CobblemonBehaviour(
            name = name,
            description = description,
            configurations = emptyList(),
            entityType = entityType
        )
        return object : Map.Entry<ResourceLocation, CobblemonBehaviour> {
            override val key = identifier
            override val value = behaviour
        }
    }

    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: Map.Entry<ResourceLocation, CobblemonBehaviour>) {
        buffer.writeIdentifier(entry.key)
        buffer.writeText(entry.value.name)
        buffer.writeText(entry.value.description)
        buffer.writeNullable(entry.value.entityType) { _, it -> buffer.writeIdentifier(it) }
    }

    override fun synchronizeDecoded(entries: Collection<Map.Entry<ResourceLocation, CobblemonBehaviour>>) {
        CobblemonBehaviours.behaviours.clear()
        CobblemonBehaviours.behaviours.putAll(entries.associate { it.key to it.value })
    }
}