/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.cooking

import com.cobblemon.mod.common.api.cooking.Seasoning
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.net.messages.client.data.DataRegistrySyncPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.writeIdentifier
import net.minecraft.network.RegistryFriendlyByteBuf

class SeasoningRegistrySyncPacket(seasonings: List<Seasoning>) :
    DataRegistrySyncPacket<Seasoning, SeasoningRegistrySyncPacket>(seasonings) {

    companion object {
        val ID = cobblemonResource("seasonings")
        fun decode(buffer: RegistryFriendlyByteBuf) =
            SeasoningRegistrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }

    override val id = ID

    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: Seasoning) {
        Seasoning.STREAM_CODEC.encode(buffer, entry)
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): Seasoning {
        return Seasoning.STREAM_CODEC.decode(buffer)
    }

    override fun synchronizeDecoded(entries: Collection<Seasoning>) {
        Seasonings.reloadEntries(entries)
    }
}
