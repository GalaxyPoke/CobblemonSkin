/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.storage.pc

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.storage.pc.PCStore
import com.cobblemon.mod.common.api.storage.pc.link.PCLink
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * Notifies a player that they must open the PC GUI for the given PC store ID. This is assuming
 * that a [PCLink] has been created that will allow them to make edits to it.
 *
 * A possible future improvement to this would be having a readOnly boolean field which will lock
 * modifying actions on the client for read-only presentation of PCs.
 *
 * Handled by [com.cobblemon.mod.common.client.net.storage.pc.OpenPCHandler]
 *
 * @author Hiroku
 * @since June 20th, 2022
 */
class OpenPCPacket : NetworkPacket<OpenPCPacket> {
    val storeID: UUID
    val box: Int?
    val unseenWallpapers: Set<ResourceLocation>

    @JvmOverloads
    @Deprecated("Use the constructor with the PCStore object instead, this will become private in a future title update", level = DeprecationLevel.WARNING)
    constructor(storeID: UUID, box: Int? = null, unseenWallpapers: Set<ResourceLocation> = emptySet()) {
        this.storeID = storeID
        this.box = box
        this.unseenWallpapers = unseenWallpapers
    }

    @JvmOverloads
    constructor(pc: PCStore, box: Int? = null): this(pc.uuid, box, pc.unseenWallpapers)

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(storeID)
        buffer.writeNullable(box) { buf, value -> buf.writeInt(value)}
        buffer.writeCollection(unseenWallpapers) { _, it -> buffer.writeResourceLocation(it) }
    }

    companion object {
        val ID = cobblemonResource("open_pc")
        fun decode(buffer: RegistryFriendlyByteBuf) = OpenPCPacket(
            buffer.readUUID(),
            buffer.readNullable { it.readInt() },
            buffer.readList { buffer.readResourceLocation() }.toSet()
        )
    }
}