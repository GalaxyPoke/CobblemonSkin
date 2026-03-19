/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mechanics

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.readExpressionLike
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.resolveInt
import com.cobblemon.mod.common.util.writeExpressionLike
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

class RemediesMechanic {
    val remedies = mutableMapOf<String, RemedyEntry>()

    internal fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeMap(this.remedies,
            { _, key -> buffer.writeString(key) },
            { _, entry ->
                buffer.writeExpressionLike(entry.healingAmount)
                buffer.writeExpressionLike(entry.friendshipDrop)
            }
        )
    }

    companion object {
        internal fun decode(buffer: RegistryFriendlyByteBuf): RemediesMechanic {
            val mechanic = RemediesMechanic()

            val decodedRemedies = buffer.readMap(
                { buffer.readString() },
                {
                    val healingExpression = buffer.readExpressionLike()
                    val friendshipExpression = buffer.readExpressionLike()
                    RemedyEntry(healingExpression, friendshipExpression)
                }
            )

            mechanic.remedies.clear()
            mechanic.remedies.putAll(decodedRemedies)

            return mechanic
        }
    }

    fun getHealingAmount(type: String, runtime: MoLangRuntime, default: Int = 20) = remedies[type]?.let { runtime.resolveInt(it.healingAmount) } ?: default
    fun getFriendshipDrop(type: String, runtime: MoLangRuntime, default: Int = 0) = remedies[type]?.let { runtime.resolveInt(it.friendshipDrop) } ?: default
}

data class RemedyEntry(val healingAmount: ExpressionLike, val friendshipDrop: ExpressionLike)