/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.server.storage.pc

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.UnsplittablePacket
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readSizedInt
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeSizedInt
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import java.util.UUID

class RequestRenamePCBoxPacket internal constructor(val storeID: UUID, val boxNumber: Int, val name: String?) : NetworkPacket<RequestRenamePCBoxPacket>, UnsplittablePacket {

    override val id = ID

    override fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeUUID(storeID)
        buffer.writeSizedInt(IntSize.U_SHORT, boxNumber)
        buffer.writeString(name ?: "")
    }

    companion object {
        val ID = cobblemonResource("request_rename_pc_box")
        fun decode(buffer: RegistryFriendlyByteBuf): RequestRenamePCBoxPacket {
            val storeID = buffer.readUUID()
            val boxNumber = buffer.readSizedInt(IntSize.U_SHORT)
            val name = buffer.readString()
            return RequestRenamePCBoxPacket(storeID, boxNumber, name)
        }
    }
}