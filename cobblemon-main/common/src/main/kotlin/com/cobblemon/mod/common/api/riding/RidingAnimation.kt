/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.riding

import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

class RidingAnimation(
    val fileName: String,
    val animationName: String
) {
    fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeString(fileName)
        buffer.writeString(animationName)
    }

    companion object {
        fun decode(buffer: RegistryFriendlyByteBuf) : RidingAnimation {
            return RidingAnimation(
                buffer.readString(),
                buffer.readString()
            )
        }
    }
}
