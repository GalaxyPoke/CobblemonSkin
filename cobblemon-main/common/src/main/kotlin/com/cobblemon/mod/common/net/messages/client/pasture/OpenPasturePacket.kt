/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.pasture

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.pasture.PasturePermissions
import com.cobblemon.mod.common.client.net.pasture.OpenPastureHandler
import com.cobblemon.mod.common.entity.pokemon.PokemonBehaviourFlag
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.*
import java.util.UUID
import net.minecraft.world.item.ItemStack
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

/**
 * Opens a pasture GUI using the provided data.
 *
 * Handled by [OpenPastureHandler].
 *
 * @author Hiroku
 * @since April 9th, 2023
 */
class OpenPasturePacket(val pcId: UUID, val pastureId: UUID, val limit: Int, val tetheredPokemon: List<PasturePokemonDataDTO>, val permissions: PasturePermissions) : NetworkPacket<OpenPasturePacket> {
    class PasturePokemonDataDTO(
        val pokemonId: UUID,
        val playerId: UUID,
        val displayName: Component,
        val ownerName: String?,
        val species: ResourceLocation,
        val aspects: Set<String>,
        val heldItem: ItemStack,
        val level: Int,
        val entityKnown: Boolean,
        var behaviourFlags: Set<PokemonBehaviourFlag> = emptySet()
    ) {
        companion object {
            fun decode(buffer: RegistryFriendlyByteBuf): PasturePokemonDataDTO {
                val pokemonId = buffer.readUUID()
                val playerId = buffer.readUUID()
                val displayName = buffer.readText()
                val ownerName = buffer.readNullable { it.readString() }
                val species = buffer.readIdentifier()
                val aspects = buffer.readList { it.readString() }.toSet()
                val heldItem = buffer.readItemStack()
                val level = buffer.readSizedInt(IntSize.U_SHORT)
                val entityKnown = buffer.readBoolean()
                val behaviourFlags = buffer.readList { it.readEnumConstant(PokemonBehaviourFlag::class.java) }.toSet()

                return PasturePokemonDataDTO(
                    pokemonId = pokemonId,
                    playerId = playerId,
                    displayName = displayName,
                    ownerName = ownerName,
                    species = species,
                    aspects = aspects,
                    heldItem = heldItem,
                    level = level,
                    entityKnown = entityKnown,
                    behaviourFlags = behaviourFlags
                )
            }
        }

        fun encode(buffer: RegistryFriendlyByteBuf) {
            buffer.writeUUID(pokemonId)
            buffer.writeUUID(playerId)
            buffer.writeText(displayName)
            buffer.writeNullable(ownerName) { _, v -> buffer.writeString(v) }
            buffer.writeIdentifier(species)
            buffer.writeCollection(aspects) { _, v -> buffer.writeString(v) }
            buffer.writeItemStack(heldItem)
            buffer.writeSizedInt(IntSize.U_SHORT, level)
            buffer.writeBoolean(entityKnown)
            buffer.writeCollection(behaviourFlags) { _, flag -> buffer.writeEnumConstant(flag) }
        }
    }

    companion object {
        val ID = cobblemonResource("open_pasture")

        fun decode(buffer: RegistryFriendlyByteBuf): OpenPasturePacket {
            val pcId = buffer.readUUID()
            val pastureId = buffer.readUUID()
            val limit = buffer.readSizedInt(IntSize.U_BYTE)
            val dtos = mutableListOf<PasturePokemonDataDTO>()
            repeat(times = buffer.readSizedInt(IntSize.U_BYTE)) {
                dtos.add(PasturePokemonDataDTO.decode(buffer))
            }
            val permissions = PasturePermissions.decode(buffer)
            return OpenPasturePacket(pcId, pastureId, limit, dtos, permissions)
        }
    }

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(pcId)
        buffer.writeUUID(pastureId)
        buffer.writeSizedInt(IntSize.U_BYTE, limit)
        buffer.writeSizedInt(IntSize.U_BYTE, tetheredPokemon.size)
        for (tethered in tetheredPokemon) {
            tethered.encode(buffer)
        }
        permissions.encode(buffer)
    }
}
